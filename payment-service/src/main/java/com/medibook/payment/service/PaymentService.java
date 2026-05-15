package com.medibook.payment.service;

import com.medibook.payment.client.AuthClient;
import com.medibook.payment.client.ProviderClient;
import com.medibook.payment.dto.EarningsResponse;
import com.medibook.payment.dto.PaymentRequest;
import com.medibook.payment.entity.Payment;
import com.medibook.payment.enums.PaymentMode;
import com.medibook.payment.enums.PaymentStatus;
import com.medibook.payment.gateway.PaymentGatewayService;
import com.medibook.payment.repository.PaymentRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final AuthClient authClient;
    private final ProviderClient providerClient;

    @Value("${payment.cancellation-window-hours:24}")
    private int cancellationWindowHours;

    // --- CREATE PAYMENT ---
    @Transactional
    public Map<String, Object> createPayment(Long appointmentId, Long patientId,
                                              Long providerId, Double amount, String paymentMode) {
        PaymentMode mode = PaymentMode.valueOf(paymentMode.toUpperCase());

        Payment payment = Payment.builder()
                .appointmentId(appointmentId)
                .patientId(patientId)
                .providerId(providerId)
                .amount(amount)
                .mode(mode)
                .status(PaymentStatus.PENDING)
                .currency("INR")
                .build();

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
        log.info("Payment created: id={} appointment={} mode={} amount=₹{}", payment.getId(), appointmentId, mode, amount);

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

    // --- CONFIRM PAYMENT ---
    @Transactional
    public Map<String, Object> confirmPayment(Long paymentId, PaymentRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING)
            throw new RuntimeException("Payment is not in PENDING state");

        Map<String, Object> verifyResult = paymentGatewayService.verifyPayment(
                payment.getGatewayOrderId(),
                request != null ? request.getGatewayPaymentId() : null,
                request != null ? request.getGatewaySignature() : null);

        Map<String, Object> response = new LinkedHashMap<>();
        if (Boolean.TRUE.equals(verifyResult.get("success"))) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setTransactionId((String) verifyResult.get("transactionId"));
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);
            response.put("success", true);
            response.put("paymentId", payment.getId());
            response.put("appointmentId", payment.getAppointmentId());
            response.put("transactionId", payment.getTransactionId());
            response.put("status", "PAID");
            response.put("message", "Payment successful!");
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            response.put("success", false);
            response.put("paymentId", payment.getId());
            response.put("appointmentId", payment.getAppointmentId());
            response.put("status", "FAILED");
            response.put("message", "Payment verification failed");
        }
        return response;
    }

    // --- REFUND (patient cancellation — checks 24h window) ---
    @Transactional
    public Map<String, Object> refundPayment(Long appointmentId) {
        Map<String, Object> response = new LinkedHashMap<>();
        Optional<Payment> optPayment = paymentRepository.findByAppointmentId(appointmentId);
        if (optPayment.isEmpty()) {
            response.put("refunded", false);
            response.put("message", "No payment found");
            return response;
        }

        Payment payment = optPayment.get();

        if (payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(PaymentStatus.CANCELLED);
            payment.setNotes("Payment cancelled — appointment cancelled before payment");
            paymentRepository.save(payment);
            response.put("refunded", false);
            response.put("message", "Unpaid payment has been cancelled");
            return response;
        }

        if (payment.getStatus() != PaymentStatus.PAID) {
            response.put("refunded", false);
            response.put("message", "Payment is not in PAID status, cannot refund");
            return response;
        }

        if (payment.getMode() == PaymentMode.CASH) {
            response.put("refunded", false);
            response.put("message", "Cash payments cannot be refunded online");
            return response;
        }

        // Note: In microservices, appointment date check would need a Feign call
        // For now, use createdAt + 24h as fallback since appointment data is in another service
        LocalDateTime deadline = payment.getCreatedAt().plusHours(cancellationWindowHours);
        // This is a simplification; in production, fetch appointment date from appointment-service
        if (LocalDateTime.now().isAfter(deadline)) {
            response.put("refunded", false);
            response.put("message", String.format("Refund not available. Cancellation must be at least %d hours before appointment.", cancellationWindowHours));
            return response;
        }

        return processRefundViaGateway(payment, response);
    }

    // --- REFUND BY PROVIDER (rejection — no time window) ---
    @Transactional
    public Map<String, Object> refundPaymentByProvider(Long appointmentId) {
        Map<String, Object> response = new LinkedHashMap<>();
        Optional<Payment> optPayment = paymentRepository.findByAppointmentId(appointmentId);
        if (optPayment.isEmpty()) {
            response.put("refunded", false);
            response.put("message", "No payment found");
            return response;
        }

        Payment payment = optPayment.get();

        if (payment.getStatus() == PaymentStatus.REFUNDED || payment.getStatus() == PaymentStatus.CANCELLED) {
            response.put("refunded", false);
            response.put("message", "Payment already processed");
            return response;
        }

        if (payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(PaymentStatus.CANCELLED);
            payment.setNotes("Payment cancelled — appointment rejected by doctor");
            paymentRepository.save(payment);
            response.put("refunded", false);
            response.put("message", "Unpaid payment has been cancelled");
            return response;
        }

        if (payment.getMode() == PaymentMode.CASH) {
            payment.setStatus(PaymentStatus.CANCELLED);
            payment.setNotes("Cash payment cancelled — appointment rejected by doctor");
            paymentRepository.save(payment);
            response.put("refunded", false);
            response.put("message", "Cash payment has been cancelled");
            return response;
        }

        // ALWAYS mark as REFUNDED
        boolean gatewaySuccess = false;
        try {
            Map<String, Object> refundResult = paymentGatewayService.processRefund(payment.getTransactionId(), payment.getAmount());
            gatewaySuccess = Boolean.TRUE.equals(refundResult.get("success"));
            payment.setNotes(gatewaySuccess ? "Refund processed: " + refundResult.get("refundId")
                    : "Refund marked (gateway failed). Manual processing may be required.");
        } catch (Exception e) {
            payment.setNotes("Refund marked (gateway error: " + e.getMessage() + "). Manual processing may be required.");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        response.put("refunded", true);
        response.put("amount", payment.getAmount());
        response.put("message", gatewaySuccess ? "Payment refunded successfully" : "Payment marked as refunded. Gateway processing pending.");
        return response;
    }

    // --- CANCEL PENDING ---
    @Transactional
    public void cancelPendingPayment(Long appointmentId) {
        paymentRepository.findByAppointmentId(appointmentId).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.CANCELLED);
                payment.setNotes("Payment cancelled — appointment cancelled");
                paymentRepository.save(payment);
            }
        });
    }

    // --- MARK CASH PAID ---
    @Transactional
    public void markCashPaymentPaid(Long appointmentId) {
        paymentRepository.findByAppointmentId(appointmentId).ifPresent(payment -> {
            if (payment.getMode() == PaymentMode.CASH && payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.PAID);
                payment.setPaidAt(LocalDateTime.now());
                payment.setTransactionId("cash_" + UUID.randomUUID().toString().substring(0, 8));
                payment.setNotes("Paid at clinic after consultation");
                paymentRepository.save(payment);
            }
        });
    }

    // --- PAYMENT HISTORY ---
    public List<Map<String, Object>> getPaymentHistory(Long patientId) {
        return paymentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::toPaymentMap).toList();
    }

    // --- GET BY APPOINTMENT ---
    public Map<String, Object> getPaymentByAppointmentId(Long appointmentId) {
        Payment payment = paymentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("No payment found for this appointment"));
        return toPaymentMap(payment);
    }

    // --- INVOICE ---
    public Map<String, Object> generateInvoice(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        Map<String, Object> invoice = new LinkedHashMap<>();
        invoice.put("invoiceNumber", "INV-" + String.format("%06d", payment.getId()));
        invoice.put("paymentId", payment.getId());
        invoice.put("transactionId", payment.getTransactionId());

        // Fetch names via Feign
        try {
            Map<String, Object> patient = authClient.getUserById(payment.getPatientId());
            invoice.put("patientName", patient.getOrDefault("name", "Patient"));
            invoice.put("patientEmail", patient.getOrDefault("email", ""));
        } catch (Exception e) {
            invoice.put("patientName", "Patient");
            invoice.put("patientEmail", "");
        }

        try {
            Map<String, Object> provider = providerClient.getProviderById(payment.getProviderId());
            invoice.put("providerName", provider.getOrDefault("userName", "Doctor"));
            invoice.put("specialization", provider.getOrDefault("specialization", ""));
            invoice.put("clinicName", provider.getOrDefault("clinicName", ""));
        } catch (Exception e) {
            invoice.put("providerName", "Doctor");
            invoice.put("specialization", "");
            invoice.put("clinicName", "");
        }

        invoice.put("amount", payment.getAmount());
        invoice.put("currency", payment.getCurrency());
        invoice.put("paymentMode", payment.getMode().name());
        invoice.put("paymentStatus", payment.getStatus().name());
        invoice.put("paidAt", payment.getPaidAt());
        invoice.put("createdAt", payment.getCreatedAt());
        return invoice;
    }

    // --- TOTAL REVENUE (admin) ---
    public Map<String, Object> getTotalRevenue() {
        List<Payment> all = paymentRepository.findAll();
        double totalPaid = 0, totalRefunded = 0;
        int paidCount = 0, refundedCount = 0, pendingCount = 0;

        for (Payment p : all) {
            switch (p.getStatus()) {
                case PAID -> { totalPaid += p.getAmount(); paidCount++; }
                case REFUNDED -> { totalRefunded += p.getAmount(); refundedCount++; }
                case PENDING -> pendingCount++;
                default -> {}
            }
        }

        Map<String, Object> revenue = new LinkedHashMap<>();
        revenue.put("totalRevenue", totalPaid);
        revenue.put("totalPaid", totalPaid);
        revenue.put("totalRefunded", totalRefunded);
        revenue.put("paidCount", paidCount);
        revenue.put("refundedCount", refundedCount);
        revenue.put("pendingCount", pendingCount);
        revenue.put("totalTransactions", all.size());
        return revenue;
    }

    // --- PROVIDER EARNINGS ---
    public EarningsResponse getProviderEarnings(Long providerId) {
        List<Payment> allProviderPayments = paymentRepository.findByProviderIdOrderByCreatedAtDesc(providerId);

        double totalPaid = 0, totalRefunded = 0, pendingAmount = 0;
        int paidCount = 0, refundedCount = 0, pendingCount = 0;

        for (Payment p : allProviderPayments) {
            switch (p.getStatus()) {
                case PAID -> { totalPaid += p.getAmount(); paidCount++; }
                case REFUNDED -> { totalRefunded += p.getAmount(); refundedCount++; }
                case PENDING -> { pendingCount++; pendingAmount += p.getAmount(); }
                default -> {}
            }
        }

        LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime monthEnd = YearMonth.now().atEndOfMonth().atTime(LocalTime.MAX);
        double monthlyEarnings = paymentRepository.findByProviderIdAndStatusAndPaidAtBetween(providerId, PaymentStatus.PAID, monthStart, monthEnd)
                .stream().mapToDouble(Payment::getAmount).sum();

        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = LocalDate.now().atTime(LocalTime.MAX);
        double dailyEarnings = paymentRepository.findByProviderIdAndStatusAndPaidAtBetween(providerId, PaymentStatus.PAID, dayStart, dayEnd)
                .stream().mapToDouble(Payment::getAmount).sum();

        List<Map<String, Object>> recentPayments = allProviderPayments.stream().limit(10).map(this::toPaymentMap).toList();

        LocalDateTime thirtyDaysAgo = LocalDate.now().minusDays(29).atStartOfDay();
        List<Payment> last30Days = paymentRepository.findByProviderIdAndStatusAndPaidAtBetween(providerId, PaymentStatus.PAID, thirtyDaysAgo, dayEnd);
        Map<LocalDate, Double> dailyMap = new TreeMap<>();
        for (Payment p : last30Days) { dailyMap.merge(p.getPaidAt().toLocalDate(), p.getAmount(), Double::sum); }
        List<Map<String, Object>> dailyBreakdown = dailyMap.entrySet().stream()
                .map(e -> { Map<String, Object> d = new LinkedHashMap<>(); d.put("date", e.getKey().toString()); d.put("amount", e.getValue()); return d; }).toList();

        return EarningsResponse.builder()
                .totalEarnings(totalPaid).monthlyEarnings(monthlyEarnings).dailyEarnings(dailyEarnings)
                .pendingPayments(pendingCount).pendingAmount(pendingAmount)
                .completedPayments(paidCount).refundedPayments(refundedCount).refundedAmount(totalRefunded)
                .recentPayments(recentPayments).dailyBreakdown(dailyBreakdown).build();
    }

    // --- HELPERS ---
    private Map<String, Object> processRefundViaGateway(Payment payment, Map<String, Object> response) {
        Map<String, Object> refundResult = paymentGatewayService.processRefund(payment.getTransactionId(), payment.getAmount());
        if (Boolean.TRUE.equals(refundResult.get("success"))) {
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundedAt(LocalDateTime.now());
            payment.setNotes("Refund processed: " + refundResult.get("refundId"));
            paymentRepository.save(payment);
            response.put("refunded", true);
            response.put("amount", payment.getAmount());
            response.put("message", refundResult.get("message"));
        } else {
            response.put("refunded", false);
            response.put("message", "Refund processing failed. Please contact support.");
        }
        return response;
    }

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
}
