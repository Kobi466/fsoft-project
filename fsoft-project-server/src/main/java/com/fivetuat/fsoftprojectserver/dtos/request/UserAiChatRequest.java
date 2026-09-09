package com.fivetuat.fsoftprojectserver.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Schema(description = "Request trò chuyện với Trợ lý AI cho học viên")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAiChatRequest {

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    @Size(max = 2000, message = "Câu hỏi không được vượt quá 2000 ký tự")
    @Schema(description = "Nội dung câu hỏi hoặc yêu cầu học tập của học viên", example = "Tạo cho tôi bộ bài từ vựng phỏng vấn IT ReactJS 10 từ")
    String message;
}
