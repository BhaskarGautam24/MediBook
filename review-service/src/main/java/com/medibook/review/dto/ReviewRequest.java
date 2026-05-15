package com.medibook.review.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Request payload for submitting or updating a review")
public class ReviewRequest {
    @Schema(description = "ID of the appointment being reviewed", example = "10")
    private Long appointmentId;
    @NotNull @Min(1) @Max(5)
    @Schema(description = "Rating from 1 to 5", example = "4")
    private Integer rating;
    @Size(max = 1000)
    @Schema(description = "Optional review comment", example = "Great doctor, very thorough!")
    private String comment;
    @Schema(description = "Whether to hide the reviewer's identity", example = "false")
    private Boolean isAnonymous;
}
