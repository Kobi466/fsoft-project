package com.fivetuat.fsoftprojectserver.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AudioReflexSubmitRequest {
    @NotNull(message = "Deck ID không được để trống")
    Long deckId;

    int score;
    Integer maxCombo;
    Integer correctAnswers;
    Integer totalQuestions;
    Double completionTime;
}
