package com.fivetuat.fsoftprojectserver.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiAutoDeckRequest {
    @NotBlank(message = "Chủ đề bộ bài không được để trống")
    String topic;
    
    @Builder.Default
    int cardCount = 15;
    
    @Builder.Default
    String sourceLanguage = "en";
    
    @Builder.Default
    String targetLanguage = "vi";
}
