package com.placement.portal.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployerProfileUpdateRequest {

    @Size(max = 255, message = "companyName must be at most 255 characters")
    private String companyName;

    @Size(max = 255, message = "companyWebsite must be at most 255 characters")
    private String companyWebsite;

    @Size(max = 100, message = "industry must be at most 100 characters")
    private String industry;

    @Pattern(
            regexp = "^(STARTUP|SMALL|MEDIUM|LARGE|ENTERPRISE)?$",
            message = "companySize must be one of STARTUP, SMALL, MEDIUM, LARGE, ENTERPRISE"
    )
    private String companySize;

    @Size(max = 255, message = "hrContactName must be at most 255 characters")
    private String hrContactName;

    @Size(max = 20, message = "hrContactPhone must be at most 20 characters")
    private String hrContactPhone;

    @Size(max = 500, message = "logoUrl must be at most 500 characters")
    private String logoUrl;

    @Size(max = 255, message = "location must be at most 255 characters")
    private String location;

    private String description;
}
