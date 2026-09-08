package com.fivetuat.fsoftprojectserver.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Schema(description = "Phản hồi trò chuyện với Trợ lý AI cho học viên")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAiChatResponse {

    @Schema(description = "ID phiên trò chuyện của học viên")
    String conversationId;

    @Schema(description = "Nội dung phản hồi từ AI")
    String reply;

    @Schema(description = "Danh sách tên các tool AI đã gọi (nếu có)")
    List<String> toolsCalled;
}
