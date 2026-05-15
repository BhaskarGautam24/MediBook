package com.medibook.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload for confirming a payment")
public class PaymentRequest {
    @Schema(description = "Gateway-generated order ID", example = "order_xyz123")
    private String gatewayOrderId;
    @Schema(description = "Gateway-generated payment ID", example = "pay_abc456")
    private String gatewayPaymentId;
    @Schema(description = "Gateway signature for verification", example = "sig_def789")
    private String gatewaySignature;
}
