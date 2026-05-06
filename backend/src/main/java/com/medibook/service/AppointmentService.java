package com.medibook.service;

import com.medibook.dto.BookingRequest;
import com.medibook.entity.Appointment;
import com.medibook.entity.Provider;
import com.medibook.entity.Slot;
import com.medibook.entity.User;
import com.medibook.enums.AppointmentStatus;
import com.medibook.enums.NotificationType;
import com.medibook.enums.PaymentMode;
import com.medibook.enums.RelatedType;
import com.medibook.repository.AppointmentRepository;
import com.medibook.repository.PaymentRepository;
import com.medibook.repository.ProviderRepository;
import com.medibook.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;
    private final ProviderRepository providerRepository;
    private final NotificationService notificationService;
    private final ReminderSchedulingService reminderSchedulingService;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    /**
     * Book an appointment (patient only).
     *
     * ONLINE PAYMENT (UPI/CARD/WALLET/NETBANKING):
     *   1. Slot is temporarily locked (isBooked = true)
     *   2. Appointment created with status = PENDING_PAYMENT
     *   3. Payment record created via gateway
     *   4. Returns paymentId + gatewayOrderId for frontend
     *   5. After payment confirmed → status becomes PENDING (normal flow)
     *
     * PAY AT CLINIC (CASH):
     *   1. Slot is booked
     *   2. Appointment created with status = PENDING (existing behavior)
     *   3. Payment record created with PENDING status
     */
    @Transactional
    public Map<String, Object> bookAppointment(User patient, BookingRequest request) {
        Slot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        // Prevent double booking
        if (Boolean.TRUE.equals(slot.getIsBooked())) {
            throw new RuntimeException("This slot is already booked");
        }

        Provider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        if (!provider.getIsVerified()) {
            throw new RuntimeException("Provider is not verified");
        }

        // Verify slot belongs to the provider
        if (!slot.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Slot does not belong to this provider");
        }

        // Determine payment mode
        String paymentModeStr = request.getPaymentMode() != null
                ? request.getPaymentMode().toUpperCase() : "CASH";
        PaymentMode paymentMode = PaymentMode.valueOf(paymentModeStr);
        boolean isOnlinePayment = (paymentMode != PaymentMode.CASH);

        // Lock the slot (temporarily for online, permanently for cash)
        slot.setIsBooked(true);
        slotRepository.save(slot);

        // Create appointment
        AppointmentStatus initialStatus = isOnlinePayment
                ? AppointmentStatus.PENDING_PAYMENT
                : AppointmentStatus.PENDING;

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .provider(provider)
                .slot(slot)
                .status(initialStatus)
                .build();
        appointment = appointmentRepository.save(appointment);

        // Create payment record
        Double consultationFee = provider.getConsultationFee() != null
                ? provider.getConsultationFee() : 500.0;
        Map<String, Object> paymentResult = paymentService.createPayment(
                appointment.getId(),
                patient.getId(),
                provider.getId(),
                consultationFee,
                paymentModeStr
        );

        // Build response
        Map<String, Object> response = toAppointmentMap(appointment);
        response.put("paymentId", paymentResult.get("paymentId"));
        response.put("gatewayOrderId", paymentResult.get("gatewayOrderId"));
        response.put("gateway", paymentResult.get("gateway"));
        response.put("paymentAmount", consultationFee);
        response.put("paymentMode", paymentModeStr);
        response.put("paymentStatus", paymentResult.get("status"));

        // For cash: notify provider immediately (same as before)
        if (!isOnlinePayment) {
            notificationService.createAndSendNotification(
                    provider.getUser().getId(),
                    NotificationType.APPOINTMENT_BOOKED,
                    "New Appointment Request",
                    String.format("You have a new appointment request from %s for %s (Pay at Clinic)",
                            patient.getName(), slot.getDate()),
                    appointment.getId(),
                    RelatedType.APPOINTMENT
            );
        }
        // For online: notification sent after payment is confirmed

        return response;
    }

    /**
     * Called by PaymentController after payment is confirmed.
     * Moves appointment from PENDING_PAYMENT → PENDING.
     */
    @Transactional
    public void onPaymentConfirmed(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            log.warn("Appointment {} not in PENDING_PAYMENT state, skipping", appointmentId);
            return;
        }

        appointment.setStatus(AppointmentStatus.PENDING);
        appointmentRepository.save(appointment);

        log.info("Appointment {} moved to PENDING after payment confirmed", appointmentId);

        // Now notify provider about new appointment
        notificationService.createAndSendNotification(
                appointment.getProvider().getUser().getId(),
                NotificationType.APPOINTMENT_BOOKED,
                "New Appointment Request (Paid Online)",
                String.format("You have a new paid appointment request from %s for %s",
                        appointment.getPatient().getName(),
                        appointment.getSlotDate()),
                appointment.getId(),
                RelatedType.APPOINTMENT
        );
    }

    /**
     * Called when online payment fails.
     * Cancels appointment and releases slot.
     */
    @Transactional
    public void onPaymentFailed(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        // Release the slot
        Slot slot = appointment.getSlot();
        if (slot != null) {
            slot.setIsBooked(false);
            slotRepository.save(slot);
        }

        log.info("Appointment {} cancelled due to payment failure", appointmentId);
    }

    /**
     * Get appointments for a patient
     */
    public List<Map<String, Object>> getPatientAppointments(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(this::toAppointmentMapWithPayment)
                .toList();
    }

    /**
     * Get appointments for a provider
     */
    public List<Map<String, Object>> getProviderAppointments(Long userId) {
        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider profile not found"));
        return appointmentRepository.findByProviderIdOrderByCreatedAtDesc(provider.getId())
                .stream()
                .map(this::toAppointmentMapWithPayment)
                .toList();
    }

    /**
     * Cancel an appointment (patient).
     * Triggers refund for online payments if within cancellation window.
     */
    @Transactional
    public Map<String, Object> cancelAppointment(Long appointmentId, Long patientId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getPatient().getId().equals(patientId)) {
            throw new RuntimeException("You can only cancel your own appointments");
        }

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED
                && appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.BOOKED
                && appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Only pending, booked, or scheduled appointments can be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        // Release the slot (if still exists)
        Slot slot = appointment.getSlot();
        if (slot != null) {
            slot.setIsBooked(false);
            slotRepository.save(slot);
        }

        // Cancel all pending reminders
        reminderSchedulingService.cancelReminders(appointmentId);
        log.info("Cancelled reminders for appointment {}", appointmentId);

        // Trigger refund for online payments
        Map<String, Object> refundResult = paymentService.refundPayment(appointmentId);
        log.info("Refund result for appointment {}: {}", appointmentId, refundResult);

        // Notify provider
        notificationService.createAndSendNotification(
                appointment.getProvider().getUser().getId(),
                NotificationType.APPOINTMENT_CANCELLED,
                "Appointment Cancelled",
                String.format("The appointment with %s on %s has been cancelled",
                        appointment.getPatient().getName(),
                        slot != null ? slot.getDate() : appointment.getSlotDate()),
                appointment.getId(),
                RelatedType.APPOINTMENT
        );

        // Build response with refund info
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Appointment cancelled successfully");
        response.put("refund", refundResult);
        return response;
    }

    /**
     * Accept an appointment (provider)
     */
    @Transactional
    public void acceptAppointment(Long appointmentId, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider profile not found"));

        if (!appointment.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("You can only accept your own appointments");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING && appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new RuntimeException("Only pending appointments can be accepted");
        }

        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appointment);

        // Schedule 24H and 1H reminders for confirmed appointment
        reminderSchedulingService.scheduleReminders(appointment);
        log.info("Scheduled reminders for accepted appointment {}", appointmentId);

        // Notify patient
        String videoLink = "http://localhost:5173/meet/" + appointment.getId();
        notificationService.createAndSendNotification(
                appointment.getPatient().getId(),
                NotificationType.APPOINTMENT_ACCEPTED,
                "Appointment Confirmed",
                String.format("Your appointment with Dr. %s on %s has been confirmed. Join Video Consultation: %s", appointment.getProvider().getUser().getName(), appointment.getSlot() != null ? appointment.getSlot().getDate() : appointment.getSlotDate(), videoLink),
                appointment.getId(),
                RelatedType.APPOINTMENT
        );
    }

    /**
     * Reject an appointment (provider)
     */
    @Transactional
    public void rejectAppointment(Long appointmentId, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider profile not found"));

        if (!appointment.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("You can only reject your own appointments");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING && appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new RuntimeException("Only pending appointments can be rejected");
        }

        appointment.setStatus(AppointmentStatus.REJECTED);
        appointmentRepository.save(appointment);

        // Release the slot (if still exists)
        Slot slot = appointment.getSlot();
        if (slot != null) {
            slot.setIsBooked(false);
            slotRepository.save(slot);
        }

        // Cancel any pending reminders
        reminderSchedulingService.cancelReminders(appointmentId);

        // Trigger refund for online payments
        Map<String, Object> refundResult = paymentService.refundPayment(appointmentId);
        log.info("Refund on rejection for appointment {}: {}", appointmentId, refundResult);

        // Notify patient
        notificationService.createAndSendNotification(
                appointment.getPatient().getId(),
                NotificationType.APPOINTMENT_REJECTED,
                "Appointment Rejected",
                String.format("Your appointment request with Dr. %s on %s was rejected",
                        appointment.getProvider().getUser().getName(),
                        slot != null ? slot.getDate() : appointment.getSlotDate()),
                appointment.getId(),
                RelatedType.APPOINTMENT
        );
    }

    /**
     * Complete an appointment (provider).
     * Auto-marks cash payments as PAID.
     */
    @Transactional
    public void completeAppointment(Long appointmentId, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider profile not found"));

        if (!appointment.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("You can only complete your own appointments");
        }

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new RuntimeException("Only scheduled appointments can be completed");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        // Release the slot so it no longer shows as "Booked"
        Slot completedSlot = appointment.getSlot();
        if (completedSlot != null) {
            completedSlot.setIsBooked(false);
            slotRepository.save(completedSlot);
        }

        // Cancel any remaining pending reminders
        reminderSchedulingService.cancelReminders(appointmentId);

        // Auto-mark cash payments as PAID on completion
        paymentService.markCashPaymentPaid(appointmentId);

        // Notify patient
        notificationService.createAndSendNotification(
                appointment.getPatient().getId(),
                NotificationType.APPOINTMENT_COMPLETED,
                "Appointment Completed",
                String.format("Your appointment with Dr. %s has been completed",
                        appointment.getProvider().getUser().getName()),
                appointment.getId(),
                RelatedType.APPOINTMENT
        );
    }

    /**
     * Get all appointments (admin)
     */
    public List<Map<String, Object>> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::toAppointmentMapWithPayment)
                .toList();
    }

    /**
     * Convert appointment to map — includes payment info if available.
     */
    private Map<String, Object> toAppointmentMapWithPayment(Appointment a) {
        Map<String, Object> map = toAppointmentMap(a);
        // Attach payment info if exists
        try {
            paymentRepository.findByAppointmentId(a.getId()).ifPresent(payment -> {
                map.put("paymentId", payment.getId());
                map.put("paymentStatus", payment.getStatus().name());
                map.put("paymentMode", payment.getMode().name());
                map.put("paymentAmount", payment.getAmount());
                map.put("transactionId", payment.getTransactionId());
            });
        } catch (Exception e) {
            // Payment might not exist for older appointments
        }
        return map;
    }

    private Map<String, Object> toAppointmentMap(Appointment a) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", a.getId());
        map.put("patientId", a.getPatient().getId());
        map.put("patientName", a.getPatient().getName());
        map.put("providerId", a.getProvider().getId());
        map.put("providerName", a.getProvider().getUser().getName());

        // Use live slot if available, otherwise fall back to snapshot fields
        if (a.getSlot() != null) {
            map.put("slotId", a.getSlot().getId());
            map.put("slotDate", a.getSlot().getDate().toString());
            map.put("slotStartTime", a.getSlot().getStartTime().toString());
            map.put("slotEndTime", a.getSlot().getEndTime().toString());
        } else {
            map.put("slotId", null);
            map.put("slotDate", a.getSlotDate() != null ? a.getSlotDate().toString() : "N/A");
            map.put("slotStartTime", a.getSlotStartTime() != null ? a.getSlotStartTime().toString() : "N/A");
            map.put("slotEndTime", a.getSlotEndTime() != null ? a.getSlotEndTime().toString() : "N/A");
        }

        map.put("status", a.getStatus() != null ? a.getStatus().name() : AppointmentStatus.PENDING.name());
        map.put("createdAt", a.getCreatedAt());
        return map;
    }
}
