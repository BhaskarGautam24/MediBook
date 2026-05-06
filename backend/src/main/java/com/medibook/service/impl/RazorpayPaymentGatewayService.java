package com.medibook.service.impl;

import com.medibook.service.PaymentGatewayService;
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

/**
 * Razorpay Payment Gateway — uses direct REST API calls.
 *
 * Bypasses the razorpay-java SDK entirely because it uses OkHttp
 * which is incompatible with Java 24 (String→char[] cast bug).
 * Instead, we call Razorpay's REST API directly via Spring RestTemplate.
 *
 * Active when payment.gateway=razorpay in application.yml.
 */
@Service
@ConditionalOnProperty(name = "payment.gateway", havingValue = "razorpay")
@Slf4j
public class RazorpayPaymentGatewayService implements PaymentGatewayService {

    private static final String RAZORPAY_API_BASE = "https://api.razorpay.com/v1";

    @Value("${payment.razorpay.key-id}")
    private String keyId;

    @Value("${payment.razorpay.key-secret}")
    private String keySecret;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        this.restTemplate = new RestTemplate();
        if (keyId != null && !keyId.isEmpty() && keySecret != null && !keySecret.isEmpty()) {
            log.info("Razorpay client initialized (direct REST mode)");
        } else {
            log.warn("Razorpay keys are not configured properly in application.yml");
        }
    }

    /**
     * Build HTTP headers with Basic Auth for Razorpay API.
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = keyId + ":" + keySecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);
        return headers;
    }

    @Override
    public Map<String, Object> createOrder(double amount, String currency, String receiptId) {
        log.info("[RAZORPAY GATEWAY] Creating order: ₹{} {} receipt={}", amount, currency, receiptId);
        Map<String, Object> result = new HashMap<>();

        try {
            // Razorpay expects amount in paise (multiply by 100)
            int amountInPaise = (int) (amount * 100);

            // Build request body
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("amount", amountInPaise);
            body.put("currency", currency);
            body.put("receipt", receiptId);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, buildHeaders());

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    RAZORPAY_API_BASE + "/orders", request, Map.class);

            if (response != null && response.containsKey("id")) {
                result.put("success", true);
                result.put("orderId", response.get("id"));
                result.put("amount", amount);
                result.put("currency", currency);
                result.put("gateway", "razorpay");
                log.info("[RAZORPAY GATEWAY] Order created successfully: {}", response.get("id"));
            } else {
                result.put("success", false);
                result.put("message", "Razorpay returned unexpected response");
                log.error("[RAZORPAY GATEWAY] Unexpected response: {}", response);
            }
        } catch (Exception e) {
            log.error("[RAZORPAY GATEWAY] Failed to create order", e);
            result.put("success", false);
            result.put("message", "Gateway error: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> verifyPayment(String gatewayOrderId, String paymentId, String signature) {
        log.info("[RAZORPAY GATEWAY] Verifying payment: orderId={}, paymentId={}", gatewayOrderId, paymentId);
        Map<String, Object> result = new HashMap<>();

        try {
            // Verify signature using HMAC SHA256
            // The signature is computed as: HMAC_SHA256(orderId + "|" + paymentId, keySecret)
            String payload = gatewayOrderId + "|" + paymentId;
            String expectedSignature = hmacSha256(payload, keySecret);

            if (expectedSignature.equals(signature)) {
                result.put("success", true);
                result.put("transactionId", paymentId);
                result.put("message", "Payment verified successfully via Razorpay");
                log.info("[RAZORPAY GATEWAY] Verification successful");
            } else {
                result.put("success", false);
                result.put("message", "Invalid payment signature");
                log.warn("[RAZORPAY GATEWAY] Verification failed - invalid signature");
            }
        } catch (Exception e) {
            log.error("[RAZORPAY GATEWAY] Verification error", e);
            result.put("success", false);
            result.put("message", "Gateway error during verification: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> processRefund(String transactionId, double amount) {
        log.info("[RAZORPAY GATEWAY] Processing refund: txn={} amount=₹{}", transactionId, amount);
        Map<String, Object> result = new HashMap<>();

        try {
            int amountInPaise = (int) (amount * 100);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("amount", amountInPaise);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, buildHeaders());

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    RAZORPAY_API_BASE + "/payments/" + transactionId + "/refund",
                    request, Map.class);

            if (response != null && response.containsKey("id")) {
                result.put("success", true);
                result.put("refundId", response.get("id"));
                result.put("amount", amount);
                result.put("message", "Refund processed successfully via Razorpay.");
                log.info("[RAZORPAY GATEWAY] Refund successful: {}", response.get("id"));
            } else {
                result.put("success", false);
                result.put("message", "Refund failed: unexpected response from Razorpay");
            }
        } catch (Exception e) {
            log.error("[RAZORPAY GATEWAY] Refund error", e);
            result.put("success", false);
            result.put("message", "Failed to process refund: " + e.getMessage());
        }

        return result;
    }

    /**
     * Compute HMAC SHA256 hex digest for Razorpay signature verification.
     */
    private String hmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // Convert to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
