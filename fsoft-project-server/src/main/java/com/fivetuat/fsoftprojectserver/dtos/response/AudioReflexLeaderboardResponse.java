package com.fivetuat.fsoftprojectserver.dtos.response;

import java.time.Instant;

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
public class AudioReflexLeaderboardResponse {
    int rank;
    String userId;
    String userName;
    String userAvatar;
    int score;
    Integer maxCombo;
    double accuracy;
    Instant playedAt;
}
