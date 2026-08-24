package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.game.AudioReflexRoomState;

public interface AudioReflexRoomService {
    AudioReflexRoomState createRoom(Long deckId, int questionCount, String hostUserId);
    AudioReflexRoomState joinRoom(String roomCode, String userId);
    AudioReflexRoomState startGame(String roomCode, String requesterId);
    AudioReflexRoomState submitAnswer(String roomCode, String userId, Long cardId);
    AudioReflexRoomState selectPlayerDeck(String roomCode, String userId, Long selectedDeckId);
    AudioReflexRoomState getRoom(String roomCode);
    AudioReflexRoomState leaveRoom(String roomCode, String userId);
    AudioReflexRoomState markDisconnected(String roomCode, String userId);
    AudioReflexRoomState markReconnected(String roomCode, String userId);
}