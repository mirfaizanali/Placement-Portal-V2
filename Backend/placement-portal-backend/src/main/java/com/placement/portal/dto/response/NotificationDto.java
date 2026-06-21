package com.placement.portal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private String id;
    private String type;
    private String title;
    private String message;
    @JsonProperty("isRead")
    private boolean isRead;
    private String referenceType;
    private String referenceId;
    private LocalDateTime createdAt;
}
