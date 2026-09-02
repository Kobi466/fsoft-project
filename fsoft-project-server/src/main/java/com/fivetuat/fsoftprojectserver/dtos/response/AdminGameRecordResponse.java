package com.fivetuat.fsoftprojectserver.dtos.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminGameRecordResponse {
    String id;
    String gameType;
    int score;
    int totalCards;
    int correctCount;
    int timeInSeconds;
    double accuracyRate;
    Long deckId;
    String deckTitle;
    String userEmail;
    String userFullName;
    LocalDateTime createdAt;
}
