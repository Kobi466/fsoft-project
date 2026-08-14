package com.fivetuat.fsoftprojectserver.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AudioReflexSubmitRequest {
    @NotNull(message = "Deck ID không được để trống")
    Long deckId;

    @NotNull(message = "Điểm số không được để trống")
    Integer score;

    Integer maxCombo;
    Integer correctAnswers;
    Integer totalQuestions;
    Double completionTime;
}