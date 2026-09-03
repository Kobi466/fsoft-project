package com.fivetuat.fsoftprojectserver.dtos.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizAnalyticsResponse {
    int totalQuizzesTaken;
    float averageAccuracyRate;
    float totalScoreEarned;
    int totalTimeSpentSeconds;
    float bestAccuracy;
    List<QuizResultResponse> recentResults;
}
