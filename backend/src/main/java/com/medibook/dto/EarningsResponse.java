package com.medibook.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * DTO for provider earnings dashboard data.
 */
@Data
@Builder
public class EarningsResponse {
    private Double totalEarnings;       // Total PAID - Total REFUNDED
    private Double monthlyEarnings;     // This month
    private Double dailyEarnings;       // Today
    private Integer pendingPayments;    // Pay-at-clinic not yet paid
    private Double pendingAmount;
    private Integer completedPayments;  // Total PAID count
    private Integer refundedPayments;   // Total REFUNDED count
    private Double refundedAmount;
    private List<Map<String, Object>> recentPayments; // Last 10 payments
}
