package com.medibook.review.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
@Schema(description = "Review response with full details")
public class ReviewResponse {
    private Long id;
    private Long appointmentId;
    private Long patientId;
    private Long providerId;
    private String patientName;
    private String providerName;
    @Schema(description = "Rating from 1 to 5", example = "4")
    private Integer rating;
    private String comment;
    private Boolean isAnonymous;
    private Boolean isVerified;
    private Boolean isFlagged;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
