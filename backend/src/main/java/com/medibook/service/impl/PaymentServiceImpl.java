package com.medibook.service.impl;

import com.medibook.dto.EarningsResponse;
import com.medibook.dto.PaymentRequest;
import com.medibook.entity.Appointment;
import com.medibook.entity.Payment;
import com.medibook.enums.PaymentMode;
import com.medibook.enums.PaymentStatus;
import com.medibook.repository.AppointmentRepository;
import com.medibook.repository.PaymentRepository;
import com.medibook.service.PaymentGatewayService;
import com.medibook.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

/**
 * Payment Service Implementation.
 * Contains all business logic for payment processing, refunds, and earnings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final PaymentGatewayService paymentGatewayService;

    // Cancellation window in hours (from application.yml)
    @Value("${payment.cancellation-window-hours:24}")
    private int cancellationWindowHours;

    // ─────────────────────────────────────────────────────────────
    // 1. CREATE PAYMENT
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public Map<String, Object> createPayment(Long appointmentId, Long patientId,
                                              Long providerId, Double amount, String paymentMode) {

        // Parse payment mode from string
        PaymentMode mode = PaymentMode.valueOf(paymentMode.toUpperCase());

        // Build payment record
        Payment payment = Payment.builder()
                .appointmentId(appointmentId)
                .patientId(patientId)
                .providerId(providerId)
                .amount(amount)
                .mode(mode)
                .status(PaymentStatus.PENDING)
                .currency("INR")
                .build();

        // For online payments: call gateway to create order
        Map<String, Object> orderResult = null;
        if (mode != PaymentMode.CASH) {
            String receiptId = "apt_" + appointmentId;
            orderResult = paymentGatewayService.createOrder(amount, "INR", receiptId);

            if (Boolean.TRUE.equals(orderResult.get("success"))) {
                payment.setGatewayOrderId((String) orderResult.get("orderId"));
            } else {
                throw new RuntimeException("Gateway error: " + orderResult.get("message"));
            }
        }

        payment = paymentRepository.save(payment);
        log.info("Payment created: id={} appointment={} mode={} amount=₹{}",
                payment.getId(), appointmentId, mode, amount);

        // Build response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("paymentId", payment.getId());
        response.put("gatewayOrderId", payment.getGatewayOrderId());
        response.put("amount", payment.getAmount());
        response.put("currency", payment.getCurrency());
        response.put("mode", payment.getMode().name());
        response.put("status", payment.getStatus().name());
        response.put("gateway", orderResult != null ? orderResult.get("gateway") : "mock");

        return response;
    }

    // ─────────────────────────────────────────────────────────────
    // 2. CONFIRM PAYMENT (after patient completes gateway flow)
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public Map<String, Object> confirmPayment(Long paymentId, PaymentRequest request) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Payment is not in PENDING state");
        }

        // Verify with gateway
        Map<String, Object> verifyResult = paymentGatewayService.verifyPayment(
                payment.getGatewayOrderId(),
                request != null ? request.getGatewayPaymentId() : null,
                request != null ? request.getGatewaySignature() : null
        );

        if (Boolean.TRUE.equals(verifyResult.get("success"))) {
            // Payment successful
            payment.setStatus(PaymentStatus.PAID);
            payment.setTransactionId((String) verifyResult.get("transactionId"));
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            log.info("Payment confirmed: id={} txn={}", paymentId, payment.getTransactionId());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("paymentId", payment.getId());
            response.put("transactionId", payment.getTransactionId());
            response.put("status", "PAID");
            response.put("message", "Payment successful!");
            return response;
        } else {
            // Payment failed
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("paymentId", payment.getId());
            response.put("status", "FAILED");
            response.put("message", "Payment verification failed");
            return response;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 3. REFUND PAYMENT (on cancellation)
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public Map<String, Object> refundPayment(Long appointmentId) {
        Map<String, Object> response = new LinkedHashMap<>();

        Optional<Payment> optPayment = paymentRepository.findByAppointmentId(appointmentId);
        if (optPayment.isEmpty()) {
            response.put("refunded", false);
            response.put("message", "No payment found for this appointment");
            return response;
        }

        Payment payment = optPayment.get();

        // Only PAID payments can be refunded
        if (payment.getStatus() != PaymentStatus.PAID) {
            response.put("refunded", false);
            response.put("message", "Payment is not in PAID status, cannot refund");
            return response;
        }

        // CASH payments are not refunded via gateway
        if (payment.getMode() == PaymentMode.CASH) {
            response.put("refunded", false);
            response.put("message", "Cash payments cannot be refunded online");
            return response;
        }

        // Check cancellation window: refund only if >= 24h before appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        LocalDateTime appointmentDateTime = getAppointmentDateTime(appointment);
        LocalDateTime cancellationDeadline = appointmentDateTime.minusHours(cancellationWindowHours);

        if (LocalDateTime.now().isAfter(cancellationDeadline)) {
            response.put("refunded", false);
            response.put("message", String.format(
                    "Refund not available. Cancellation must be at least %d hours before appointment.",
                    cancellationWindowHours));
            return response;
        }

        // Process refund via gateway
        Map<String, Object> refundResult = paymentGatewayService.processRefund(
                payment.getTransactionId(), payment.getAmount());

        if (Boolean.TRUE.equals(refundResult.get("success"))) {
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundedAt(LocalDateTime.now());
            payment.setNotes("Refund processed: " + refundResult.get("refundId"));
            paymentRepository.save(payment);

            log.info("Payment refunded: id={} amount=₹{}", payment.getId(), payment.getAmount());

            response.put("refunded", true);
            response.put("amount", payment.getAmount());
            response.put("message", (String) refundResult.get("message"));
        } else {
            response.put("refunded", false);
            response.put("message", "Refund processing failed. Please contact support.");
        }

        return response;
    }

    // ─────────────────────────────────────────────────────────────
    // 4. MARK CASH PAYMENT AS PAID (when appointment is completed)
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void markCashPaymentPaid(Long appointmentId) {
        Optional<Payment> optPayment = paymentRepository.findByAppointmentId(appointmentId);
        if (optPayment.isPresent()) {
            Payment payment = optPayment.get();
            if (payment.getMode() == PaymentMode.CASH && payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.PAID);
                payment.setPaidAt(LocalDateTime.now());
                payment.setTransactionId("cash_" + UUID.randomUUID().toString().substring(0, 8));
                payment.setNotes("Paid at clinic after consultation");
                paymentRepository.save(payment);
                log.info("Cash payment marked as PAID for appointment {}", appointmentId);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 5. GET PAYMENT HISTORY (for patient)
    // ─────────────────────────────────────────────────────────────
    @Override
    public List<Map<String, Object>> getPaymentHistory(Long patientId) {
        return paymentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(this::toPaymentMap)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────
    // 6. GET PAYMENT BY APPOINTMENT ID
    // ─────────────────────────────────────────────────────────────
    @Override
    public Map<String, Object> getPaymentByAppointmentId(Long appointmentId) {
        Payment payment = paymentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("No payment found for this appointment"));
        return toPaymentMap(payment);
    }

    // ─────────────────────────────────────────────────────────────
    // 7. GENERATE INVOICE
    // ─────────────────────────────────────────────────────────────
    @Override
    public Map<String, Object> generateInvoice(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        Appointment appointment = appointmentRepository.findById(payment.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        Map<String, Object> invoice = new LinkedHashMap<>();
        invoice.put("invoiceNumber", "INV-" + String.format("%06d", payment.getId()));
        invoice.put("paymentId", payment.getId());
        invoice.put("transactionId", payment.getTransactionId());
        invoice.put("patientName", appointment.getPatient().getName());
        invoice.put("patientEmail", appointment.getPatient().getEmail());
        invoice.put("providerName", appointment.getProvider().getUser().getName());
        invoice.put("specialization", appointment.getProvider().getSpecialization());
        invoice.put("clinicName", appointment.getProvider().getClinicName());
        invoice.put("appointmentDate", appointment.getSlotDate() != null
                ? appointment.getSlotDate().toString() : "N/A");
        invoice.put("appointmentTime", appointment.getSlotStartTime() != null
                ? appointment.getSlotStartTime().toString() + " - " + appointment.getSlotEndTime().toString()
                : "N/A");
        invoice.put("amount", payment.getAmount());
        invoice.put("currency", payment.getCurrency());
        invoice.put("paymentMode", payment.getMode().name());
        invoice.put("paymentStatus", payment.getStatus().name());
        invoice.put("paidAt", payment.getPaidAt());
        invoice.put("createdAt", payment.getCreatedAt());

        return invoice;
    }

    // ─────────────────────────────────────────────────────────────
    // 8. GET TOTAL REVENUE (admin)
    // ─────────────────────────────────────────────────────────────
    @Override
    public Map<String, Object> getTotalRevenue() {
        List<Payment> allPayments = paymentRepository.findAll();

        double totalPaid = 0;
        double totalRefunded = 0;
        int paidCount = 0;
        int refundedCount = 0;
        int pendingCount = 0;

        for (Payment p : allPayments) {
            if (p.getStatus() == PaymentStatus.PAID) {
                totalPaid += p.getAmount();
                paidCount++;
            } else if (p.getStatus() == PaymentStatus.REFUNDED) {
                totalRefunded += p.getAmount();
                refundedCount++;
            } else if (p.getStatus() == PaymentStatus.PENDING) {
                pendingCount++;
            }
        }

        Map<String, Object> revenue = new LinkedHashMap<>();
        revenue.put("totalRevenue", totalPaid - totalRefunded);
        revenue.put("totalPaid", totalPaid);
        revenue.put("totalRefunded", totalRefunded);
        revenue.put("paidCount", paidCount);
        revenue.put("refundedCount", refundedCount);
        revenue.put("pendingCount", pendingCount);
        revenue.put("totalTransactions", allPayments.size());
        return revenue;
    }

    // ─────────────────────────────────────────────────────────────
    // 9. GET PROVIDER EARNINGS
    // ─────────────────────────────────────────────────────────────
    @Override
    public EarningsResponse getProviderEarnings(Long providerId) {
        List<Payment> allProviderPayments = paymentRepository
                .findByProviderIdOrderByCreatedAtDesc(providerId);

        // Calculate total earnings = PAID - REFUNDED
        double totalPaid = 0;
        double totalRefunded = 0;
        int paidCount = 0;
        int refundedCount = 0;
        int pendingCount = 0;
        double pendingAmount = 0;

        for (Payment p : allProviderPayments) {
            switch (p.getStatus()) {
                case PAID -> { totalPaid += p.getAmount(); paidCount++; }
                case REFUNDED -> { totalRefunded += p.getAmount(); refundedCount++; }
                case PENDING -> { pendingCount++; pendingAmount += p.getAmount(); }
                default -> {}
            }
        }

        // Monthly earnings (current month)
        LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime monthEnd = YearMonth.now().atEndOfMonth().atTime(LocalTime.MAX);
        List<Payment> monthlyPayments = paymentRepository
                .findByProviderIdAndStatusAndPaidAtBetween(providerId, PaymentStatus.PAID, monthStart, monthEnd);
        double monthlyEarnings = monthlyPayments.stream().mapToDouble(Payment::getAmount).sum();

        // Daily earnings (today)
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = LocalDate.now().atTime(LocalTime.MAX);
        List<Payment> dailyPayments = paymentRepository
                .findByProviderIdAndStatusAndPaidAtBetween(providerId, PaymentStatus.PAID, dayStart, dayEnd);
        double dailyEarnings = dailyPayments.stream().mapToDouble(Payment::getAmount).sum();

        // Recent payments (last 10)
        List<Map<String, Object>> recentPayments = allProviderPayments.stream()
                .limit(10)
                .map(this::toPaymentMap)
                .toList();

        return EarningsResponse.builder()
                .totalEarnings(totalPaid - totalRefunded)
                .monthlyEarnings(monthlyEarnings)
                .dailyEarnings(dailyEarnings)
                .pendingPayments(pendingCount)
                .pendingAmount(pendingAmount)
                .completedPayments(paidCount)
                .refundedPayments(refundedCount)
                .refundedAmount(totalRefunded)
                .recentPayments(recentPayments)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // HELPER: Convert Payment to Map for JSON response
    // ─────────────────────────────────────────────────────────────
    private Map<String, Object> toPaymentMap(Payment p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("appointmentId", p.getAppointmentId());
        map.put("patientId", p.getPatientId());
        map.put("providerId", p.getProviderId());
        map.put("amount", p.getAmount());
        map.put("currency", p.getCurrency());
        map.put("status", p.getStatus().name());
        map.put("mode", p.getMode().name());
        map.put("transactionId", p.getTransactionId());
        map.put("gatewayOrderId", p.getGatewayOrderId());
        map.put("paidAt", p.getPaidAt());
        map.put("refundedAt", p.getRefundedAt());
        map.put("notes", p.getNotes());
        map.put("createdAt", p.getCreatedAt());
        return map;
    }

    // HELPER: Get appointment date-time for refund window check
    private LocalDateTime getAppointmentDateTime(Appointment appointment) {
        LocalDate date = appointment.getSlotDate();
        LocalTime time = appointment.getSlotStartTime();
        if (date != null && time != null) {
            return LocalDateTime.of(date, time);
        }
        // Fallback: if slot still exists
        if (appointment.getSlot() != null) {
            return LocalDateTime.of(appointment.getSlot().getDate(), appointment.getSlot().getStartTime());
        }
        // Last resort: use creation time + 24h
        return appointment.getCreatedAt().plusHours(24);
    }
}
