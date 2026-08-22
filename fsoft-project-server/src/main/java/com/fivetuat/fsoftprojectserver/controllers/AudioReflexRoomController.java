// controllers/AudioReflexRoomController.java
package com.fivetuat.fsoftprojectserver.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.game.AudioReflexRoomState;
import com.fivetuat.fsoftprojectserver.services.AudioReflexRoomService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/games/audio-reflex/rooms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Audio Reflex Multiplayer Room")
public class AudioReflexRoomController {

    AudioReflexRoomService roomService;

    private String currentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AudioReflexRoomState>> createRoom(
            @RequestParam Long deckId,
            @RequestParam(defaultValue = "10") int questionCount) {
        AudioReflexRoomState state = roomService.createRoom(deckId, questionCount, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<AudioReflexRoomState>builder().status(201).message("Tạo phòng thành công").data(state).build());
    }

    @PostMapping("/{code}/join")
    public ResponseEntity<ApiResponse<AudioReflexRoomState>> join(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.<AudioReflexRoomState>builder()
                .status(200).message("Vào phòng thành công")
                .data(roomService.joinRoom(code, currentUserId())).build());
    }

    @PostMapping("/{code}/start")
    public ResponseEntity<ApiResponse<AudioReflexRoomState>> start(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.<AudioReflexRoomState>builder()
                .status(200).message("Bắt đầu ván chơi")
                .data(roomService.startGame(code, currentUserId())).build());
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<AudioReflexRoomState>> get(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.<AudioReflexRoomState>builder()
                .status(200).data(roomService.getRoom(code)).build());
    }

    @PostMapping("/{code}/answer")
    public ResponseEntity<ApiResponse<AudioReflexRoomState>> submitAnswer(
            @PathVariable String code,
            @RequestBody com.fivetuat.fsoftprojectserver.dtos.request.SubmitAnswerRequest request) {
        Long cardId = request != null ? request.getCardId() : null;
        return ResponseEntity.ok(ApiResponse.<AudioReflexRoomState>builder()
                .status(200).message("Nộp câu trả lời thành công")
                .data(roomService.submitAnswer(code, currentUserId(), cardId)).build());
    }

    @PutMapping("/{code}/select-deck")
    public ResponseEntity<ApiResponse<AudioReflexRoomState>> selectDeck(
            @PathVariable String code,
            @RequestParam(required = false) Long selectedDeckId) {
        return ResponseEntity.ok(ApiResponse.<AudioReflexRoomState>builder()
                .status(200).message("Đã cập nhật bộ thẻ đóng góp")
                .data(roomService.selectPlayerDeck(code, currentUserId(), selectedDeckId)).build());
    }

    @DeleteMapping("/{code}/leave")
    public ResponseEntity<ApiResponse<Void>> leave(@PathVariable String code) {
        roomService.leaveRoom(code, currentUserId());
        return ResponseEntity.ok(ApiResponse.<Void>builder().status(200).message("Đã rời phòng").build());
    }
}