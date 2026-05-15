package com.medibook.payment.gateway;

import java.util.Map;

public interface PaymentGatewayService {
    Map<String, Object> createOrder(double amount, String currency, String receiptId);
    Map<String, Object> verifyPayment(String gatewayOrderId, String paymentId, String signature);
    Map<String, Object> processRefund(String transactionId, double amount);
}
