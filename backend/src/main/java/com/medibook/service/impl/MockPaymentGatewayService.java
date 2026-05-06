package com.medibook.service.impl;

import com.medibook.service.PaymentGatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mock Payment Gateway — simulates real payment processing.
 *
 * Active when payment.gateway=mock (or not set at all).
 * Always returns success with fake transaction IDs.
 *
 * When deploying: switch to RazorpayPaymentGatewayService
 * by setting payment.gateway=razorpay in application.yml.
 */
@Service
@ConditionalOnProperty(name = "payment.gateway", havingValue = "mock", matchIfMissing = true)
@Slf4j
public class MockPaymentGatewayService implements PaymentGatewayService {

    @Override
    public Map<String, Object> createOrder(double amount, String currency, String receiptId) {
        log.info("[MOCK GATEWAY] Creating order: ₹{} {} receipt={}", amount, currency, receiptId);

        // Generate a fake order ID (like Razorpay's order_xxxxx format)
        String orderId = "mock_order_" + UUID.randomUUID().toString().substring(0, 12);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("orderId", orderId);
        result.put("amount", amount);
        result.put("currency", currency);
        result.put("gateway", "mock");

        log.info("[MOCK GATEWAY] Order created: {}", orderId);
        return result;
    }

    @Override
    public Map<String, Object> verifyPayment(String gatewayOrderId, String paymentId, String signature) {
        log.info("[MOCK GATEWAY] Verifying payment: orderId={}", gatewayOrderId);

        // Mock always succeeds — generate fake transaction ID
        String transactionId = "mock_txn_" + UUID.randomUUID().toString().substring(0, 12);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("transactionId", transactionId);
        result.put("message", "Payment verified successfully (mock gateway)");

        log.info("[MOCK GATEWAY] Payment verified: txn={}", transactionId);
        return result;
    }

    @Override
    public Map<String, Object> processRefund(String transactionId, double amount) {
        log.info("[MOCK GATEWAY] Processing refund: txn={} amount=₹{}", transactionId, amount);

        String refundId = "mock_refund_" + UUID.randomUUID().toString().substring(0, 12);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("refundId", refundId);
        result.put("amount", amount);
        result.put("message", "Refund processed successfully. Will be credited in 3-5 business days.");

        log.info("[MOCK GATEWAY] Refund processed: {}", refundId);
        return result;
    }
}
