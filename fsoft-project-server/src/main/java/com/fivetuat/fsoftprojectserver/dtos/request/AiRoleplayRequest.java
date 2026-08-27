package com.fivetuat.fsoftprojectserver.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiRoleplayRequest {
    @Builder.Default
    String scenario = "JOB_INTERVIEW"; 
    
    List<String> targetWords;
    
    @NotBlank(message = "Tin nhắn của người dùng không được để trống")
    String userMessage;
}

