package com.fivetuat.fsoftprojectserver.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Schema(description = "Nhật ký một hành động AI đã thực hiện (audit log)")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminAiAuditLogResponse {

    @Schema(description = "ID bản ghi nhật ký")
    Long id;

    @Schema(description = "Tên hành động", example = "SET_OFFICIAL")
    String action;

    @Schema(description = "Tham số gốc của hành động (JSON)", example = "{\"deckId\":11,\"isOfficial\":true}")
    String params;

    @Schema(description = "Trạng thái thực thi: EXECUTED / CANCELLED / FAILED", example = "EXECUTED")
    String status;

    @Schema(description = "Kết quả tóm tắt hoặc thông báo lỗi", example = "DeckResponse(id=11, title=...)")
    String resultSummary;

    @Schema(description = "ID tham chiếu (deckId hoặc userId)", example = "11")
    String referenceId;

    @Schema(description = "Thời điểm ghi nhật ký")
    LocalDateTime createdAt;
}