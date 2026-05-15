package com.medibook.appointment.service;

import com.medibook.appointment.client.*;
import com.medibook.appointment.dto.BookingRequest;
import com.medibook.appointment.entity.Appointment;
import com.medibook.appointment.enums.AppointmentStatus;
import com.medibook.appointment.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AuthClient authClient;
    private final ProviderClient providerClient;
    private final ScheduleClient scheduleClient;
    private final PaymentClient paymentClient;
    private final NotificationClient notificationClient;

    // --- Book Appointment ---

    @Transactional
    public Map<String, Object> bookAppointment(Long patientId, BookingRequest request) {
        // Get slot info from schedule-service
        Map<String, Object> slot = scheduleClient.getSlotById(request.getSlotId());
        if (slot == null || slot.isEmpty()) throw new RuntimeException("Slot not found");

        if (Boolean.TRUE.equals(slot.get("booked"))) {
            throw new RuntimeException("This slot is already booked");
        }

        // Get provider info
        Map<String, Object> provider = providerClient.getProviderById(request.getProviderId());
        if (provider == null) throw new RuntimeException("Provider not found");
        if (!Boolean.TRUE.equals(provider.get("isVerified"))) throw new RuntimeException("Provider is not verified");

        // Verify slot belongs to provider
        Long slotProviderId = Long.valueOf(slot.get("providerId").toString());
        if (!slotProviderId.equals(request.getProviderId())) {
            throw new RuntimeException("Slot does not belong to this provider");
        }

        String paymentModeStr = request.getPaymentMode() != null ? request.getPaymentMode().toUpperCase() : "CASH";
        boolean isOnlinePayment = !"CASH".equals(paymentModeStr);

        // Lock the slot
        scheduleClient.updateBookingStatus(request.getSlotId(), Map.of("booked", true));

        // Create appointment
        AppointmentStatus initialStatus = isOnlinePayment ? AppointmentStatus.PENDING_PAYMENT : AppointmentStatus.PENDING;

        Appointment appointment = Appointment.builder()
                .patientId(patientId)
                .providerId(request.getProviderId())
                .slotId(request.getSlotId())
                .slotDate(LocalDate.parse(slot.get("date").toString()))
                .slotStartTime(LocalTime.parse(slot.get("startTime").toString()))
                .slotEndTime(LocalTime.parse(slot.get("endTime").toString()))
                .status(initialStatus)
                .build();
        appointment = appointmentRepository.save(appointment);

        // Create payment
        Double consultationFee = provider.get("consultationFee") != null
                ? Double.valueOf(provider.get("consultationFee").toString()) : 500.0;

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("appointmentId", appointment.getId());
        paymentData.put("patientId", patientId);
        paymentData.put("providerId", request.getProviderId());
        paymentData.put("amount", consultationFee);
        paymentData.put("paymentMode", paymentModeStr);

        Map<String, Object> paymentResult;
        try {
            paymentResult = paymentClient.createPayment(paymentData);
        } catch (Exception e) {
            log.warn("Failed to create payment: {}", e.getMessage());
            paymentResult = Map.of("paymentId", 0, "status", "PENDING");
        }

        // Build response
        Map<String, Object> response = toAppointmentMap(appointment);
        response.put("paymentId", paymentResult.get("paymentId"));
        response.put("gatewayOrderId", paymentResult.get("gatewayOrderId"));
        response.put("gateway", paymentResult.get("gateway"));
        response.put("paymentAmount", consultationFee);
        response.put("paymentMode", paymentModeStr);
        response.put("paymentStatus", paymentResult.get("status"));

        // Notify provider for cash payments
        if (!isOnlinePayment) {
            sendNotification(Long.valueOf(provider.get("userId").toString()),
                    "APPOINTMENT_BOOKED", "New Appointment Request",
                    String.format("New appointment request from patient for %s (Pay at Clinic)", slot.get("date")),
                    appointment.getId(), "APPOINTMENT");
        }

        return response;
    }

    // --- Payment Confirmed ---

    @Transactional
    public void onPaymentConfirmed(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) return;

        appointment.setStatus(AppointmentStatus.PENDING);
        appointmentRepository.save(appointment);

        Map<String, Object> provider = providerClient.getProviderById(appointment.getProviderId());
        sendNotification(Long.valueOf(provider.get("userId").toString()),
                "APPOINTMENT_BOOKED", "New Appointment Request (Paid Online)",
                String.format("New paid appointment request for %s", appointment.getSlotDate()),
                appointment.getId(), "APPOINTMENT");
    }

    // --- Payment Failed ---

    @Transactional
    public void onPaymentFailed(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        if (appointment.getSlotId() != null) {
            try { scheduleClient.updateBookingStatus(appointment.getSlotId(), Map.of("booked", false)); }
            catch (Exception e) { log.warn("Failed to release slot: {}", e.getMessage()); }
        }
    }

    // --- Get Single Appointment (for internal service calls) ---

    public Map<String, Object> getAppointmentById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        return toAppointmentMap(appointment);
    }

    // --- Get Appointments ---

    public List<Map<String, Object>> getPatientAppointments(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::toAppointmentMapWithPayment).toList();
    }

    public List<Map<String, Object>> getProviderAppointments(Long userId) {
        Map<String, Object> provider = providerClient.getProviderByUserId(userId);
        Long providerId = Long.valueOf(provider.get("id").toString());
        return appointmentRepository.findByProviderIdOrderByCreatedAtDesc(providerId)
                .stream().map(this::toAppointmentMapWithPayment).toList();
    }

    // --- Cancel Appointment ---

    @Transactional
    public Map<String, Object> cancelAppointment(Long appointmentId, Long patientId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getPatientId().equals(patientId)) throw new RuntimeException("You can only cancel your own appointments");

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED
                && appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.BOOKED
                && appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Only pending, booked, or scheduled appointments can be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        if (appointment.getSlotId() != null) {
            try { scheduleClient.updateBookingStatus(appointment.getSlotId(), Map.of("booked", false)); }
            catch (Exception e) { log.warn("Failed to release slot: {}", e.getMessage()); }
        }

        Map<String, Object> refundResult = Map.of("refunded", false);
        try { refundResult = paymentClient.refundPayment(appointmentId); }
        catch (Exception e) { log.warn("Refund failed: {}", e.getMessage()); }

        Map<String, Object> provider = providerClient.getProviderById(appointment.getProviderId());
        sendNotification(Long.valueOf(provider.get("userId").toString()),
                "APPOINTMENT_CANCELLED", "Appointment Cancelled",
                String.format("Appointment on %s has been cancelled by patient", appointment.getSlotDate()),
                appointment.getId(), "APPOINTMENT");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Appointment cancelled successfully");
        response.put("refund", refundResult);
        return response;
    }

    // --- Accept Appointment ---

    @Transactional
    public void acceptAppointment(Long appointmentId, Long userId) {
        Appointment appointment = getProviderOwnAppointment(appointmentId, userId);

        if (appointment.getStatus() != AppointmentStatus.PENDING && appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new RuntimeException("Only pending appointments can be accepted");
        }

        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appointment);

        Map<String, Object> provider = providerClient.getProviderById(appointment.getProviderId());
        String providerName = (String) provider.getOrDefault("userName", "Doctor");
        String videoLink = "http://localhost:5173/meet/" + appointment.getId();

        sendNotification(appointment.getPatientId(),
                "APPOINTMENT_ACCEPTED", "Appointment Confirmed",
                String.format("Your appointment with Dr. %s on %s has been confirmed. Join: %s",
                        providerName, appointment.getSlotDate(), videoLink),
                appointment.getId(), "APPOINTMENT");
    }

    // --- Reject Appointment ---

    @Transactional
    public void rejectAppointment(Long appointmentId, Long userId) {
        Appointment appointment = getProviderOwnAppointment(appointmentId, userId);

        if (appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.BOOKED
                && appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Only pending appointments can be rejected");
        }

        appointment.setStatus(AppointmentStatus.REJECTED);
        appointmentRepository.save(appointment);

        if (appointment.getSlotId() != null) {
            try { scheduleClient.updateBookingStatus(appointment.getSlotId(), Map.of("booked", false)); }
            catch (Exception e) { log.warn("Failed to release slot: {}", e.getMessage()); }
        }

        Map<String, Object> refundResult = Map.of("refunded", false);
        try { refundResult = paymentClient.refundPaymentByProvider(appointmentId); }
        catch (Exception e) { log.warn("Refund on rejection failed: {}", e.getMessage()); }

        Map<String, Object> provider = providerClient.getProviderById(appointment.getProviderId());
        String providerName = (String) provider.getOrDefault("userName", "Doctor");
        String dateStr = appointment.getSlotDate() != null ? appointment.getSlotDate().toString() : "N/A";

        String message = Boolean.TRUE.equals(refundResult.get("refunded"))
                ? String.format("Your appointment with Dr. %s on %s was rejected. Payment of ₹%s refunded.",
                    providerName, dateStr, refundResult.get("amount"))
                : String.format("Your appointment with Dr. %s on %s was rejected.", providerName, dateStr);

        sendNotification(appointment.getPatientId(), "APPOINTMENT_REJECTED", "Appointment Rejected",
                message, appointment.getId(), "APPOINTMENT");
    }

    // --- Complete Appointment ---

    @Transactional
    public void completeAppointment(Long appointmentId, Long userId) {
        Appointment appointment = getProviderOwnAppointment(appointmentId, userId);

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new RuntimeException("Only scheduled appointments can be completed");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        if (appointment.getSlotId() != null) {
            try { scheduleClient.updateBookingStatus(appointment.getSlotId(), Map.of("booked", false)); }
            catch (Exception e) { log.warn("Failed to release slot: {}", e.getMessage()); }
        }

        try { paymentClient.markCashPaymentPaid(appointmentId); }
        catch (Exception e) { log.warn("Failed to mark cash payment: {}", e.getMessage()); }

        Map<String, Object> provider = providerClient.getProviderById(appointment.getProviderId());
        String providerName = (String) provider.getOrDefault("userName", "Doctor");

        sendNotification(appointment.getPatientId(), "APPOINTMENT_COMPLETED", "Appointment Completed",
                String.format("Your appointment with Dr. %s has been completed", providerName),
                appointment.getId(), "APPOINTMENT");
    }

    // --- Handle Slot Expired (called by schedule-service) ---

    @Transactional
    public void handleSlotExpired(Long slotId) {
        List<Appointment> appointments = appointmentRepository.findBySlotId(slotId);
        for (Appointment appt : appointments) {
            switch (appt.getStatus()) {
                case SCHEDULED -> {
                    appt.setStatus(AppointmentStatus.COMPLETED);
                    appt.setSlotId(null);
                    appointmentRepository.save(appt);
                    sendNotification(appt.getPatientId(), "APPOINTMENT_COMPLETED", "Appointment Auto-Completed",
                            "Your appointment on " + appt.getSlotDate() + " has been automatically completed.",
                            appt.getId(), "APPOINTMENT");
                }
                case PENDING, BOOKED -> {
                    appt.setStatus(AppointmentStatus.CANCELLED);
                    appt.setSlotId(null);
                    appointmentRepository.save(appt);
                    sendNotification(appt.getPatientId(), "APPOINTMENT_CANCELLED", "Appointment Expired",
                            "We apologize. Your appointment on " + appt.getSlotDate() + " could not be confirmed. Please book another slot.",
                            appt.getId(), "APPOINTMENT");
                }
                case COMPLETED -> {
                    appt.setSlotId(null);
                    appointmentRepository.save(appt);
                }
                case CANCELLED, REJECTED, NO_SHOW -> {
                    appt.setSlotId(null);
                    appointmentRepository.save(appt);
                }
                default -> {}
            }
        }
        log.info("Handled {} appointments for expired slot {}", appointments.size(), slotId);
    }

    // --- Admin ---

    public List<Map<String, Object>> getAllAppointments() {
        return appointmentRepository.findAll().stream().map(this::toAppointmentMapWithPayment).toList();
    }

    // --- Helpers ---

    private Appointment getProviderOwnAppointment(Long appointmentId, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        Map<String, Object> provider = providerClient.getProviderByUserId(userId);
        Long providerId = Long.valueOf(provider.get("id").toString());
        if (!appointment.getProviderId().equals(providerId)) {
            throw new RuntimeException("You can only manage your own appointments");
        }
        return appointment;
    }

    private void sendNotification(Long recipientId, String type, String title, String message, Long relatedId, String relatedType) {
        try {
            notificationClient.sendNotification(Map.of(
                    "recipientId", recipientId, "type", type, "title", title,
                    "message", message, "relatedId", relatedId, "relatedType", relatedType
            ));
        } catch (Exception e) {
            log.warn("Failed to send notification: {}", e.getMessage());
        }
    }

    private Map<String, Object> toAppointmentMapWithPayment(Appointment a) {
        Map<String, Object> map = toAppointmentMap(a);
        try {
            Map<String, Object> payment = paymentClient.getPaymentByAppointment(a.getId());
            if (payment != null && !payment.isEmpty()) {
                map.put("paymentId", payment.get("id"));
                map.put("paymentStatus", payment.get("status"));
                map.put("paymentMode", payment.get("mode"));
                map.put("paymentAmount", payment.get("amount"));
                map.put("transactionId", payment.get("transactionId"));
            }
        } catch (Exception e) { /* Payment may not exist */ }
        return map;
    }

    private Map<String, Object> toAppointmentMap(Appointment a) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", a.getId());
        map.put("patientId", a.getPatientId());
        map.put("providerId", a.getProviderId());

        // Fetch names
        String patientName = "Patient";
        String providerName = "Doctor";
        try {
            Map<String, Object> patient = authClient.getUserById(a.getPatientId());
            patientName = (String) patient.getOrDefault("name", "Patient");
        } catch (Exception e) { /* fallback */ }
        try {
            Map<String, Object> provider = providerClient.getProviderById(a.getProviderId());
            providerName = (String) provider.getOrDefault("userName", "Doctor");
        } catch (Exception e) { /* fallback */ }

        map.put("patientName", patientName);
        map.put("providerName", providerName);
        map.put("slotId", a.getSlotId());
        map.put("slotDate", a.getSlotDate() != null ? a.getSlotDate().toString() : "N/A");
        map.put("slotStartTime", a.getSlotStartTime() != null ? a.getSlotStartTime().toString() : "N/A");
        map.put("slotEndTime", a.getSlotEndTime() != null ? a.getSlotEndTime().toString() : "N/A");
        map.put("status", a.getStatus() != null ? a.getStatus().name() : "PENDING");
        map.put("createdAt", a.getCreatedAt());
        return map;
    }
}
