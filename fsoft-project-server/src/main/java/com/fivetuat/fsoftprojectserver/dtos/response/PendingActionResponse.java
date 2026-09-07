package com.fivetuat.fsoftprojectserver.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Schema(description = "Hành động nguy hiểm đã soạn, chờ admin xác nhận qua /confirm")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PendingActionResponse {
    @Schema(description = "Dùng cho body của /confirm và /confirm/cancel")
    String actionId;

    @Schema(description = "Tên tool soạn hành động", example = "requestRejectDeck")
    String tool;

    @Schema(description = "Mô tả hành động bằng tiếng Việt", example = "Từ chối bộ bài #12 với lý do: spam")
    String summary;

    @Schema(description = "Hạn xác nhận (5 phút từ lúc soạn)")
    LocalDateTime expiresAt;
}
