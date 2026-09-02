package com.fivetuat.fsoftprojectserver.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Yêu cầu tạo nguyên bộ bài Flashcard tự động bằng AI trong 3s")
public class AiAutoDeckRequest {
    @NotBlank(message = "Chủ đề bộ bài không được để trống")
    @Schema(description = "Chủ đề từ vựng muốn AI biên soạn bộ bài", example = "Từ vựng phỏng vấn IT ReactJS")
    String topic;
    
    @Schema(description = "Số lượng thẻ bài muốn sinh (Mặc định 15, tối đa 30)", example = "15")
    @Builder.Default
    int cardCount = 15;
    
    @Schema(description = "Mã ngôn ngữ nguồn (Default: en)", example = "en")
    @Builder.Default
    String sourceLanguage = "en";
    
    @Schema(description = "Mã ngôn ngữ đích dịch nghĩa (Default: vi)", example = "vi")
    @Builder.Default
    String targetLanguage = "vi";
}
