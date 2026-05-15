package com.medibook.payment.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@ConditionalOnProperty(name = "payment.gateway", havingValue = "mock", matchIfMissing = true)
@Slf4j
public class MockPaymentGatewayService implements PaymentGatewayService {

    @Override
    public Map<String, Object> createOrder(double amount, String currency, String receiptId) {
        log.info("[MOCK GATEWAY] Creating order: ₹{} {} receipt={}", amount, currency, receiptId);
        String orderId = "mock_order_" + UUID.randomUUID().toString().substring(0, 12);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("orderId", orderId);
        result.put("amount", amount);
        result.put("currency", currency);
        result.put("gateway", "mock");
        return result;
    }

    @Override
    public Map<String, Object> verifyPayment(String gatewayOrderId, String paymentId, String signature) {
        log.info("[MOCK GATEWAY] Verifying payment: orderId={}", gatewayOrderId);
        String transactionId = "mock_txn_" + UUID.randomUUID().toString().substring(0, 12);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("transactionId", transactionId);
        result.put("message", "Payment verified successfully (mock gateway)");
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
        return result;
    }
}
