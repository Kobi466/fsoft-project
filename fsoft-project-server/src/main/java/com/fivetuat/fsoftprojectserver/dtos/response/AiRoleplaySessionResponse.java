package com.fivetuat.fsoftprojectserver.dtos.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiRoleplaySessionResponse {
    String id;
    String title;
    String scenario;
    List<String> targetWords;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
