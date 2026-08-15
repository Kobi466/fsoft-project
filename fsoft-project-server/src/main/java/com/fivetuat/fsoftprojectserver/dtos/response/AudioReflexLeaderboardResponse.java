package com.fivetuat.fsoftprojectserver.dtos.response;

import java.time.Instant;

import lombok.*;
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
    Integer score;
    Integer maxCombo;
    Double accuracy;
    Instant playedAt;
}