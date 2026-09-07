package com.fivetuat.fsoftprojectserver.dtos.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminAiAuditLogResponse {
    Long id;
    String action;
    String params;
    String status;
    String resultSummary;
    String referenceId;
    LocalDateTime createdAt;
}