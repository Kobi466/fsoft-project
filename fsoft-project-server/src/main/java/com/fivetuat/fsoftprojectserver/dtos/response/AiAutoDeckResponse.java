package com.fivetuat.fsoftprojectserver.dtos.response;

import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Kết quả bộ bài Flashcard hoàn chỉnh do AI tự động biên soạn")
public class AiAutoDeckResponse {
    @Schema(description = "Tên tiêu đề bộ bài chuẩn SEO do AI gợi ý", example = "Từ vựng phỏng vấn IT ReactJS Core")
    String title;

    @Schema(description = "Mô tả ngắn về nội dung và mục tiêu học tập của bộ bài", example = "Tổng hợp 15 từ vựng cốt lõi thường xuất hiện nhất trong các buổi phỏng vấn ReactJS.")
    String description;

    @Schema(description = "Mã ngôn ngữ nguồn", example = "en")
    String sourceLanguage;

    @Schema(description = "Mã ngôn ngữ đích", example = "vi")
    String targetLanguage;

    @Schema(description = "Danh sách các thẻ từ vựng hoàn chỉnh (Từ, Phiên âm IPA, Nghĩa, Câu ví dụ)")
    List<CardCreationRequest> cards;
}
