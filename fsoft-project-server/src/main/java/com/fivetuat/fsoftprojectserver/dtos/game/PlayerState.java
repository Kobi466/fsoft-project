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
    Long selectedDeckId;
    String selectedDeckTitle;
    int selectedDeckCardCount;
    @Builder.Default
    boolean connected = true;
}