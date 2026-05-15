package com.medibook.record.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Schema(description = "Request payload for creating or updating a medical record")
public class MedicalRecordRequest {
    @NotNull @Schema(description = "ID of the completed appointment", example = "15")
    private Long appointmentId;
    @NotBlank @Schema(description = "Medical diagnosis", example = "Acute bronchitis")
    private String diagnosis;
    @NotBlank @Schema(description = "Prescription details", example = "Amoxicillin 500mg, 3x daily for 7 days")
    private String prescription;
    @Schema(description = "Additional clinical notes")
    private String notes;
    @Schema(description = "Follow-up date if required (YYYY-MM-DD)", example = "2026-07-01")
    private LocalDate followUpDate;
}
