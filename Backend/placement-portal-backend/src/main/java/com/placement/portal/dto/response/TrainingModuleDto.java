package com.placement.portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingModuleDto {

    private String id;
    private String title;
    private String description;
    private String learningLink;
    private String iconName;
    private Integer displayOrder;
}
