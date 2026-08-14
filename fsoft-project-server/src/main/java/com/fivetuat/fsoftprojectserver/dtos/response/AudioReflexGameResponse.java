package com.fivetuat.fsoftprojectserver.dtos.response;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AudioReflexGameResponse {
    Long deckId;
    String deckTitle;
    int totalQuestions;
    List<AudioQuestionItem> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AudioQuestionItem {
        Long cardId;
        String word;              // Từ tiếng Anh (dùng card.getWord())
        String phonetic;          // Phiên âm (dùng card.getPhonetic())
        String audioUrl;          // Link phát âm (dùng card.getAudioUrl())
        String imageUrl;          // Ảnh minh họa (dùng card.getImageUrl())
        String exampleSentence;   // Câu ví dụ tiếng Anh (dùng card.getExampleSentence())
        String exampleMeaning;    // Dịch câu ví dụ (dùng card.getExampleMeaning())
        Long correctOptionId;     // ID đáp án đúng
        List<OptionItem> options; // 4 đáp án lựa chọn A, B, C, D
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OptionItem {
        Long optionId;
        String text; // Nghĩa tiếng Việt (dùng card.getMeaning())
    }
}