package com.fivetuat.fsoftprojectserver.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Đoạn văn / Email công việc hoàn chỉnh do AI tạo ra từ từ vựng nợ SRS")
public class AiStoryResponse {
    @Schema(description = "Tiêu đề bài đọc / Chủ đề email", example = "Urgent Project Proposal & Negotiation Timeline")
    String title;

    @Schema(description = "Nội dung đoạn văn Tiếng Anh chứa các từ vựng mục tiêu", example = "Dear Team, We need to reach a consensus regarding the upcoming deadline...")
    String storyText;

    @Schema(description = "Bản dịch Tiếng Việt chuẩn ngữ cảnh", example = "Thưa Team, Chúng ta cần đạt được sự đồng thuận liên quan đến hạn chót...")
    String translationText;

    @Schema(description = "Ngữ cảnh văn bản", example = "BUSINESS_EMAIL")
    String contextType;

    @Schema(description = "Danh sách các từ vựng mục tiêu đã lồng ghép thành công", example = "[\"consensus\", \"deadline\", \"proposal\"]")
    List<String> targetWords;
}
