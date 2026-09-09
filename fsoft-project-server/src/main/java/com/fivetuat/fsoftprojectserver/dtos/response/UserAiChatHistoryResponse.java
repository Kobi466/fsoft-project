package com.fivetuat.fsoftprojectserver.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Schema(description = "Một tin nhắn trong lịch sử hội thoại AI của học viên")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAiChatHistoryResponse {

    @Schema(description = "Vai trò gửi tin nhắn: user / assistant / system", example = "user")
    String role;

    @Schema(description = "Nội dung tin nhắn", example = "Tạo cho tôi một bộ bài từ vựng IELTS")
    String content;
}
