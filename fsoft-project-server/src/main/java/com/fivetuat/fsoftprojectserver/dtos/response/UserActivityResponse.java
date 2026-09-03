package com.fivetuat.fsoftprojectserver.dtos.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserActivityResponse {
    Long id;
    LocalDate activityDate;
    int cardsReviewedCount;
    int quizzesCompletedCount;
    int gamesPlayedCount;
}
