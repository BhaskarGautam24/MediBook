package com.medibook.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Schema(description = "Provider earnings summary")
public class EarningsResponse {
    @Schema(description = "Total lifetime earnings", example = "15000.00")
    private Double totalEarnings;
    @Schema(description = "Earnings for current month", example = "3000.00")
    private Double monthlyEarnings;
    @Schema(description = "Earnings for current day", example = "150.00")
    private Double dailyEarnings;
    @Schema(description = "Number of pending payments", example = "2")
    private Integer pendingPayments;
    @Schema(description = "Total amount pending", example = "300.00")
    private Double pendingAmount;
    @Schema(description = "Number of completed payments", example = "45")
    private Integer completedPayments;
    @Schema(description = "Number of refunded payments", example = "1")
    private Integer refundedPayments;
    @Schema(description = "Total amount refunded", example = "100.00")
    private Double refundedAmount;
    @Schema(description = "Recent payment transactions")
    private List<Map<String, Object>> recentPayments;
    @Schema(description = "Daily breakdown of earnings")
    private List<Map<String, Object>> dailyBreakdown;
}
