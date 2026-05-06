package com.medibook.service;

import com.medibook.dto.EarningsResponse;
import com.medibook.dto.PaymentRequest;

import java.util.List;
import java.util.Map;

/**
 * Payment Service Interface.
 * Defines all payment business logic operations.
 */
public interface PaymentService {

    /**
     * Create a new payment record when appointment is booked.
     * For ONLINE: calls gateway to create order, returns orderId.
     * For CASH: creates payment with PENDING status directly.
     */
    Map<String, Object> createPayment(Long appointmentId, Long patientId,
                                       Long providerId, Double amount, String paymentMode);

    /**
     * Confirm/process a payment after patient completes gateway flow.
     * Verifies with gateway, updates status to PAID.
     */
    Map<String, Object> confirmPayment(Long paymentId, PaymentRequest request);

    /**
     * Refund a payment (triggered on appointment cancellation).
     * Checks eligibility (24h window), processes via gateway.
     */
    Map<String, Object> refundPayment(Long appointmentId);

    /**
     * Mark a cash payment as PAID (when appointment is completed).
     */
    void markCashPaymentPaid(Long appointmentId);

    /**
     * Get payment history for a patient.
     */
    List<Map<String, Object>> getPaymentHistory(Long patientId);

    /**
     * Get payment details for a specific appointment.
     */
    Map<String, Object> getPaymentByAppointmentId(Long appointmentId);

    /**
     * Generate a simple invoice for a payment.
     */
    Map<String, Object> generateInvoice(Long paymentId);

    /**
     * Get total revenue across all providers (admin).
     */
    Map<String, Object> getTotalRevenue();

    /**
     * Get earnings for a specific provider.
     */
    EarningsResponse getProviderEarnings(Long providerId);
}
