package com.fivetuat.fsoftprojectserver.controllers;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.game.SpaceStrikerRoomState;
import com.fivetuat.fsoftprojectserver.dtos.request.SubmitAnswerRequest;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.SpaceStrikerRoomService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/games/space-striker/rooms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Space Striker Multiplayer Room", description = "Quản lý phòng chơi game Space Striker nhiều người")
public class SpaceStrikerRoomController {

    SpaceStrikerRoomService roomService;

    private String currentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PostMapping
    @Operation(summary = "Tạo phòng chơi mới")
    public ApiResponse<SpaceStrikerRoomState> createRoom(
            @RequestParam Long deckId,
            @RequestParam(defaultValue = "10") int questionCount) {
        SpaceStrikerRoomState state = roomService.createRoom(deckId, questionCount, currentUserId());
        return ApiResponse.ok(state, SuccessCode.CREATE_ROOM_SUCCESS);
    }

    @PostMapping("/{code}/join")
    @Operation(summary = "Tham gia vào phòng bằng mã code")
    public ApiResponse<SpaceStrikerRoomState> join(@PathVariable String code) {
        return ApiResponse.ok(roomService.joinRoom(code, currentUserId()), SuccessCode.JOIN_ROOM_SUCCESS);
    }

    @PostMapping("/{code}/start")
    @Operation(summary = "Bắt đầu ván chơi")
    public ApiResponse<SpaceStrikerRoomState> start(
            @PathVariable String code,
            @RequestParam(required = false) Integer questionCount) {
        return ApiResponse.ok(roomService.startGame(code, currentUserId(), questionCount), SuccessCode.START_ROOM_SUCCESS);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Lấy trạng thái thông tin phòng chơi")
    public ApiResponse<SpaceStrikerRoomState> get(@PathVariable String code) {
        return ApiResponse.ok(roomService.getRoom(code), SuccessCode.GET_ROOM_SUCCESS);
    }

    @PostMapping("/{code}/answer")
    @Operation(summary = "Nộp câu trả lời/phát bắn")
    public ApiResponse<SpaceStrikerRoomState> submitAnswer(
            @PathVariable String code,
            @RequestBody SubmitAnswerRequest request) {
        Long cardId = request != null ? request.getCardId() : null;
        Integer questionIndex = request != null ? request.getQuestionIndex() : null;
        return ApiResponse.ok(roomService.submitAnswer(code, currentUserId(), cardId, questionIndex), SuccessCode.SUBMIT_ANSWER_SUCCESS);
    }

    @PutMapping("/{code}/select-deck")
    @Operation(summary = "Chọn bộ thẻ đóng góp cho phòng chơi")
    public ApiResponse<SpaceStrikerRoomState> selectDeck(
            @PathVariable String code,
            @RequestParam(required = false) Long selectedDeckId) {
        return ApiResponse.ok(roomService.selectPlayerDeck(code, currentUserId(), selectedDeckId), SuccessCode.SELECT_DECK_SUCCESS);
    }

    @DeleteMapping("/{code}/leave")
    @Operation(summary = "Rời khỏi phòng chơi")
    public ApiResponse<Void> leave(@PathVariable String code) {
        roomService.leaveRoom(code, currentUserId());
        return ApiResponse.ok(null, SuccessCode.LEAVE_ROOM_SUCCESS);
    }
}