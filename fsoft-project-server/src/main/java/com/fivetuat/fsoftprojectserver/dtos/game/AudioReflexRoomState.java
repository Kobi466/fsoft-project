package com.fivetuat.fsoftprojectserver.dtos.game;

import java.util.*;
import com.fivetuat.fsoftprojectserver.enums.RoomStatus;
import com.fivetuat.fsoftprojectserver.dtos.response.AudioReflexGameResponse.AudioQuestionItem;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AudioReflexRoomState {
    String roomCode;
    Long deckId;
    String hostUserId;
    RoomStatus status;
    int questionCount;
    List<AudioQuestionItem> questions;
    int currentQuestionIndex;
    long startedAt;
    long currentQuestionStartedAt;
    Map<String, PlayerState> players;
    Set<String> answeredThisQuestion;
}