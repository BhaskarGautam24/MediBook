package com.medibook.service.impl;

import com.medibook.service.PaymentGatewayService;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "payment.gateway", havingValue = "stripe")
@Slf4j
public class StripePaymentGatewayService implements PaymentGatewayService {

    @Value("${payment.stripe.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        if (secretKey != null && !secretKey.isEmpty()) {
            Stripe.apiKey = secretKey;
            log.info("Stripe configured");
        } else {
            log.warn("Stripe secret key is not configured properly in application.yml");
        }
    }

    @Override
    public Map<String, Object> createOrder(double amount, String currency, String receiptId) {
        log.info("[STRIPE GATEWAY] Creating payment intent: {} {} receipt={}", amount, currency, receiptId);
        Map<String, Object> result = new HashMap<>();

        try {
            // Stripe expects amount in smallest currency unit (e.g., paise for INR)
            long amountInSmallestUnit = (long) (amount * 100);

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInSmallestUnit)
                    .setCurrency(currency.toLowerCase())
                    .putMetadata("receipt", receiptId)
                    // You can add more configurations here depending on requirements
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            result.put("success", true);
            // Stripe returns a client_secret which the frontend uses to confirm the payment
            // We'll pass the intent ID as orderId and the client_secret so the frontend can use it
            result.put("orderId", intent.getId());
            result.put("clientSecret", intent.getClientSecret());
            result.put("amount", amount);
            result.put("currency", currency);
            result.put("gateway", "stripe");

            log.info("[STRIPE GATEWAY] PaymentIntent created successfully: {}", intent.getId());
        } catch (Exception e) {
            log.error("[STRIPE GATEWAY] Failed to create payment intent", e);
            result.put("success", false);
            result.put("message", "Gateway error: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> verifyPayment(String gatewayOrderId, String paymentId, String signature) {
        // For Stripe, usually the frontend confirms the PaymentIntent and backend just retrieves it to check status.
        // `gatewayOrderId` is our intent.getId() from createOrder.
        // `paymentId` is passed by frontend as well (usually same as intent id or payment method id).
        log.info("[STRIPE GATEWAY] Verifying payment intent: {}", gatewayOrderId);
        Map<String, Object> result = new HashMap<>();

        try {
            PaymentIntent intent = PaymentIntent.retrieve(gatewayOrderId);

            if ("succeeded".equals(intent.getStatus())) {
                result.put("success", true);
                // We'll use the intent ID as the transaction ID or latest charge ID
                result.put("transactionId", intent.getLatestCharge() != null ? intent.getLatestCharge() : intent.getId());
                result.put("message", "Payment verified successfully via Stripe");
                log.info("[STRIPE GATEWAY] Verification successful");
            } else {
                result.put("success", false);
                result.put("message", "Payment is not in succeeded state. Current status: " + intent.getStatus());
                log.warn("[STRIPE GATEWAY] Verification failed - intent status: {}", intent.getStatus());
            }
        } catch (Exception e) {
            log.error("[STRIPE GATEWAY] Verification error", e);
            result.put("success", false);
            result.put("message", "Gateway error during verification: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> processRefund(String transactionId, double amount) {
        log.info("[STRIPE GATEWAY] Processing refund: txn={} amount={}", transactionId, amount);
        Map<String, Object> result = new HashMap<>();

        try {
            long amountInSmallestUnit = (long) (amount * 100);

            RefundCreateParams params = RefundCreateParams.builder()
                    // transactionId could be the PaymentIntent ID or the Charge ID. Both work for refunds in Stripe.
                    .setPaymentIntent(transactionId)
                    .setAmount(amountInSmallestUnit)
                    .build();

            Refund refund = Refund.create(params);

            if ("succeeded".equals(refund.getStatus()) || "pending".equals(refund.getStatus())) {
                result.put("success", true);
                result.put("refundId", refund.getId());
                result.put("amount", amount);
                result.put("message", "Refund processed successfully via Stripe.");
                log.info("[STRIPE GATEWAY] Refund successful: {}", refund.getId());
            } else {
                result.put("success", false);
                result.put("message", "Refund status: " + refund.getStatus());
                log.warn("[STRIPE GATEWAY] Refund warning - status: {}", refund.getStatus());
            }
        } catch (Exception e) {
            log.error("[STRIPE GATEWAY] Refund error", e);
            result.put("success", false);
            result.put("message", "Failed to process refund: " + e.getMessage());
        }

        return result;
    }
}
