package com.fivetuat.fsoftprojectserver.dtos.https.ai.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Response trả về toàn bộ cấu trúc bài học tình huống (Câu giao tiếp & Từ vựng)")
public class SituationalLearningResponse {
    @Schema(description = "Các hành động chính", example = "[{\"english\": \"I woke up early\", \"vietnamese\": \"Tôi dậy sớm\"}]")
    @JsonProperty("mainActions")
    List<SentenceOption> mainActions;
    
    @Schema(description = "Các tương tác giao tiếp", example = "[{\"english\": \"Good morning\", \"vietnamese\": \"Chào buổi sáng\"}]")
    @JsonProperty("interactions")
    List<SentenceOption> interactions;
    
    @Schema(description = "Các câu cảm xúc", example = "[{\"english\": \"I am happy\", \"vietnamese\": \"Tôi rất vui\"}]")
    @JsonProperty("emotions")
    List<SentenceOption> emotions;
    
    @Schema(description = "Các câu cảm thán/caption ngắn gọn", example = "[{\"english\": \"What a day!\", \"vietnamese\": \"Thật là một ngày tuyệt vời\"}]")
    @JsonProperty("shortCaptions")
    List<SentenceOption> shortCaptions;
    
    @Schema(description = "Danh sách thẻ từ vựng (Flashcards) rút ra từ bài học, sẵn sàng để Frontend lưu vào Deck")
    @JsonProperty("vocabularies")
    List<CardCreationRequest> vocabularies;
}
