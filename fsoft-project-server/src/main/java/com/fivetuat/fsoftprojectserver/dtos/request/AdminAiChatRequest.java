package com.fivetuat.fsoftprojectserver.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Schema(description = "Request hỏi trợ lý AI quản trị")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminAiChatRequest {

    @NotBlank(message = "Câu hỏi không được để trống")
    @Size(max = 2000, message = "Câu hỏi không được vượt quá 2000 ký tự")
    @Schema(description = "Câu hỏi bằng ngôn ngữ tự nhiên", example = "Có bao nhiêu bộ bài đang chờ duyệt?")
    String message;

    @Schema(description = "Bỏ qua — server tự suy ra thread theo admin hiện tại", example = "admin-ai-3fa85f64-...")
    String conversationId;
}
