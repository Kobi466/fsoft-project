package com.fivetuat.fsoftprojectserver.dtos.game;
import lombok.*;
import lombok.experimental.FieldDefaults;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlayerState {
    String userId;
    String displayName;
    String avatar;
    boolean host;
    int score;
    int correctCount;
    long lastAnswerAt;
    @Builder.Default
    boolean connected = true;
}