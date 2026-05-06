package com.medibook.service;

import java.util.Map;

/**
 * Payment Gateway abstraction.
 *
 * Currently implemented by MockPaymentGatewayService.
 * When deploying with real Razorpay:
 *   1. Create RazorpayPaymentGatewayService implementing this interface
 *   2. Add @ConditionalOnProperty(name="payment.gateway", havingValue="razorpay")
 *   3. Set payment.gateway=razorpay in application.yml
 *   4. Add razorpay key-id and key-secret in application.yml
 */
public interface PaymentGatewayService {

    /**
     * Create an order with the payment gateway.
     * Returns map with: success (boolean), orderId, amount, currency
     */
    Map<String, Object> createOrder(double amount, String currency, String receiptId);

    /**
     * Verify/confirm a payment.
     * Returns map with: success (boolean), transactionId, message
     */
    Map<String, Object> verifyPayment(String gatewayOrderId, String paymentId, String signature);

    /**
     * Process a refund.
     * Returns map with: success (boolean), refundId, amount, message
     */
    Map<String, Object> processRefund(String transactionId, double amount);
}
