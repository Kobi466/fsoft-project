package com.fivetuat.fsoftprojectserver.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Schema(description = "Phản hồi của trợ lý AI quản trị")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminAiChatResponse {
    @Schema(description = "ID thread hội thoại của admin")
    String conversationId;

    @Schema(description = "Câu trả lời của AI")
    String reply;

    @Schema(description = "Hành động đang chờ xác nhận (null nếu không có)")
    PendingActionResponse pendingAction;

    @Schema(description = "Kết quả thực thi hành động (chỉ có trong response của confirm, null nếu không có)")
    String result;
}
