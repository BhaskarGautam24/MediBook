package com.medibook.dto;

import lombok.Data;

/**
 * DTO for confirming a payment.
 * For mock gateway: fields can be empty (auto-generated).
 * For real Razorpay: frontend sends razorpay order/payment/signature.
 */
@Data
public class PaymentRequest {
    private String gatewayOrderId;
    private String gatewayPaymentId;
    private String gatewaySignature;
}
