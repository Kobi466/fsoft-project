package com.fivetuat.fsoftprojectserver.dtos.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateAiRoleplaySessionRequest {
    String title;
    String scenario;
    List<String> targetWords;
}
