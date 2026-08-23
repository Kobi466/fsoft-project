package com.fivetuat.fsoftprojectserver.dtos.game;

import java.util.List;
import java.util.Map;

import com.fivetuat.fsoftprojectserver.enums.RoomStatus;
import com.fivetuat.fsoftprojectserver.dtos.response.AudioReflexGameResponse.AudioQuestionItem;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpaceStrikerRoomState {
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

    /** true khi đã có người bắn trúng câu hiện tại -> phát bắn sau đó (dù trúng) không còn được tính điểm. */
    boolean questionResolved;
}