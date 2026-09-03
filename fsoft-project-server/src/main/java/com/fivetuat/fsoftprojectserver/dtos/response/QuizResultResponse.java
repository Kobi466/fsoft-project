package com.fivetuat.fsoftprojectserver.dtos.response;

import com.fivetuat.fsoftprojectserver.enums.QuizScope;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizResultResponse {
    Long id;
    QuizScope scope;
    int totalQuestions;
    int correctAnswers;
    float score;
    float accuracyRate;
    int timeSpentSeconds;
    int xpEarned;
    boolean isPerfectScore;
    LocalDateTime createdAt;
}
