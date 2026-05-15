package com.medibook.provider.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request payload for updating provider profile")
public class ProviderProfileUpdateRequest {
    @Size(max = 100)
    @Schema(description = "Medical specialization", example = "Cardiologist")
    private String specialization;
    @Size(max = 500)
    @Schema(description = "Medical qualifications", example = "MBBS, MD")
    private String qualifications;
    @Size(max = 2000)
    @Schema(description = "Provider biography", example = "Experienced cardiologist with 15+ years...")
    private String bio;
    @Min(0)
    @Schema(description = "Years of experience", example = "15")
    private Integer experienceYears;
    @Size(max = 200)
    @Schema(description = "Name of the clinic", example = "Heart Care Clinic")
    private String clinicName;
    @Size(max = 500)
    @Schema(description = "Address of the clinic", example = "123 Main St, City")
    private String clinicAddress;
    @Min(0)
    @Schema(description = "Fee per consultation", example = "500.0")
    private Double consultationFee;
}
