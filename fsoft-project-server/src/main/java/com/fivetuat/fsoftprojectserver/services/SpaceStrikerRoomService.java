package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.game.SpaceStrikerRoomState;

public interface SpaceStrikerRoomService {
    SpaceStrikerRoomState createRoom(Long deckId, int questionCount, String hostUserId);
    SpaceStrikerRoomState joinRoom(String roomCode, String userId);
    SpaceStrikerRoomState startGame(String roomCode, String requesterId, Integer questionCount);
    SpaceStrikerRoomState submitAnswer(String roomCode, String userId, Long cardId);
    SpaceStrikerRoomState submitAnswer(String roomCode, String userId, Long cardId, Integer questionIndex);
    SpaceStrikerRoomState selectPlayerDeck(String roomCode, String userId, Long selectedDeckId);
    SpaceStrikerRoomState getRoom(String roomCode);
    SpaceStrikerRoomState leaveRoom(String roomCode, String userId);
    SpaceStrikerRoomState markDisconnected(String roomCode, String userId);
    SpaceStrikerRoomState markReconnected(String roomCode, String userId);
}