package com.medibook.payment.gateway;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@ConditionalOnProperty(name = "payment.gateway", havingValue = "razorpay")
@Slf4j
public class RazorpayPaymentGatewayService implements PaymentGatewayService {

    private static final String RAZORPAY_API_BASE = "https://api.razorpay.com/v1";

    @Value("${payment.razorpay.key-id}") private String keyId;
    @Value("${payment.razorpay.key-secret}") private String keySecret;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        this.restTemplate = new RestTemplate();
        log.info("Razorpay client initialized (direct REST mode)");
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = keyId + ":" + keySecret;
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8)));
        return headers;
    }

    @Override
    public Map<String, Object> createOrder(double amount, String currency, String receiptId) {
        Map<String, Object> result = new HashMap<>();
        try {
            int amountInPaise = (int) (amount * 100);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("amount", amountInPaise);
            body.put("currency", currency);
            body.put("receipt", receiptId);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    RAZORPAY_API_BASE + "/orders", new HttpEntity<>(body, buildHeaders()), Map.class);

            if (response != null && response.containsKey("id")) {
                result.put("success", true);
                result.put("orderId", response.get("id"));
                result.put("amount", amount);
                result.put("currency", currency);
                result.put("gateway", "razorpay");
            } else {
                result.put("success", false);
                result.put("message", "Razorpay returned unexpected response");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Gateway error: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> verifyPayment(String gatewayOrderId, String paymentId, String signature) {
        Map<String, Object> result = new HashMap<>();
        try {
            String payload = gatewayOrderId + "|" + paymentId;
            String expectedSignature = hmacSha256(payload, keySecret);
            if (expectedSignature.equals(signature)) {
                result.put("success", true);
                result.put("transactionId", paymentId);
                result.put("message", "Payment verified successfully via Razorpay");
            } else {
                result.put("success", false);
                result.put("message", "Invalid payment signature");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Gateway error during verification: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> processRefund(String transactionId, double amount) {
        Map<String, Object> result = new HashMap<>();
        try {
            int amountInPaise = (int) (amount * 100);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("amount", amountInPaise);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    RAZORPAY_API_BASE + "/payments/" + transactionId + "/refund",
                    new HttpEntity<>(body, buildHeaders()), Map.class);

            if (response != null && response.containsKey("id")) {
                result.put("success", true);
                result.put("refundId", response.get("id"));
                result.put("amount", amount);
                result.put("message", "Refund processed successfully via Razorpay.");
            } else {
                result.put("success", false);
                result.put("message", "Refund failed: unexpected response");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to process refund: " + e.getMessage());
        }
        return result;
    }

    private String hmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) { String h = Integer.toHexString(0xff & b); if (h.length() == 1) hex.append('0'); hex.append(h); }
        return hex.toString();
    }
}
