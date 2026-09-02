package com.fivetuat.fsoftprojectserver.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Yêu cầu tạo đoạn văn / Email thực tế từ từ vựng SRS nợ")
public class AiStoryRequest {
    @Schema(description = "Danh sách ID các thẻ bài nợ FSRS (Tùy chọn)", example = "[101, 102, 103]")
    List<Long> cardIds;

    @Schema(description = "Danh sách các từ vựng Tiếng Anh trực tiếp (Tùy chọn)", example = "[\"negotiation\", \"consensus\", \"deadline\"]")
    List<String> words;

    @Schema(description = "Ngữ cảnh tạo văn bản (BUSINESS_EMAIL, DAILY_STORY, NEWS_ARTICLE)", example = "BUSINESS_EMAIL")
    @Builder.Default
    String contextType = "BUSINESS_EMAIL";
}
