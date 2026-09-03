package com.fivetuat.fsoftprojectserver.dtos.request;

import com.fivetuat.fsoftprojectserver.enums.QuizScope;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizSubmitRequest {
    @NotNull(message = "Scope không được để trống")
    QuizScope scope;

    int totalQuestions;

    int correctAnswers;

    float score;

    float accuracyRate;

    int timeSpentSeconds;
}
