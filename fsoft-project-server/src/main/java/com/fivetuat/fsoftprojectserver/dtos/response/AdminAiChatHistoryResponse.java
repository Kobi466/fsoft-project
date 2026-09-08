package com.fivetuat.fsoftprojectserver.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Schema(description = "Một tin nhắn trong lịch sử hội thoại AI của admin")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminAiChatHistoryResponse {

    @Schema(description = "Vai trò gửi tin nhắn: user / assistant / system", example = "assistant")
    String role;

    @Schema(description = "Nội dung tin nhắn", example = "Tổng số user trong hệ thống hiện tại là 25.")
    String content;
}