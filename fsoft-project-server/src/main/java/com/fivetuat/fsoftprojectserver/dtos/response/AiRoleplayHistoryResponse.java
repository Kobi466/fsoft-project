package com.fivetuat.fsoftprojectserver.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Chi tiết một tin nhắn trong lịch sử hội thoại đối thoại nhập vai")
public class AiRoleplayHistoryResponse {
    @Schema(description = "Vai trò gửi tin nhắn (USER: Học viên, ASSISTANT: Gia sư AI)", example = "USER")
    String role;

    @Schema(description = "Nội dung câu nói hoặc câu đáp lời", example = "In my last project, I reached a consensus with my team to hit the deadline.")
    String content;

    @Schema(description = "Mốc thời gian gửi tin nhắn chuẩn CSDL PostgreSQL", example = "2026-08-27T21:23:41")
    LocalDateTime createdAt;
}
