package com.fivetuat.fsoftprojectserver.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.AudioReflexSubmitRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AudioReflexGameResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AudioReflexLeaderboardResponse;
import com.fivetuat.fsoftprojectserver.services.AudioReflexGameService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/games/audio-reflex")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Audio Reflex Game Controller")
public class AudioReflexGameController {

    AudioReflexGameService audioReflexGameService;

    @GetMapping("/play/{deckId}")
    @Operation(summary = "Tạo bộ câu hỏi nghe phản xạ cho 1 người chơi")
    public ResponseEntity<ApiResponse<AudioReflexGameResponse>> startGame(
            @PathVariable Long deckId,
            @RequestParam(defaultValue = "10") int questionCount
    ) {
        return ResponseEntity.ok(
                ApiResponse.<AudioReflexGameResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Tạo ván chơi thành công")
                        .data(audioReflexGameService.generateGame(deckId, questionCount))
                        .build()
        );
    }

    @PostMapping("/submit")
    @Operation(summary = "Nộp kết quả ván chơi và lưu điểm số")
    public ResponseEntity<ApiResponse<AudioReflexLeaderboardResponse>> submitResult(
            @RequestBody @Valid AudioReflexSubmitRequest request
    ) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<AudioReflexLeaderboardResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Lưu kết quả thành công")
                        .data(audioReflexGameService.submitResult(request, currentUserId))
                        .build()
        );
    }

    @GetMapping("/leaderboard/{deckId}")
    @Operation(summary = "Lấy Bảng xếp hạng điểm số game phản xạ nghe")
    public ResponseEntity<ApiResponse<List<AudioReflexLeaderboardResponse>>> getLeaderboard(
            @PathVariable Long deckId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(
                ApiResponse.<List<AudioReflexLeaderboardResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Lấy bảng xếp hạng thành công")
                        .data(audioReflexGameService.getLeaderboard(deckId, limit))
                        .build()
        );
    }
}
