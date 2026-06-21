package com.placement.portal.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingModuleRequest {

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    @Size(max = 1000)
    private String description;

    @NotBlank
    @Size(max = 500)
    @Pattern(regexp = "^https?://.*", message = "learningLink must start with http:// or https://")
    private String learningLink;

    @NotBlank
    @Size(max = 60)
    private String iconName;

    @NotNull
    @Min(0)
    private Integer displayOrder;
}
