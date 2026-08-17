package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.UserActivityResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.UserStreakResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Progress Controller", description = "Quản lý tiến độ và thống kê học tập (Streak, Contribution)")
public class ProgressController {

    ProgressService progressService;

    @GetMapping("/streak")
    @Operation(summary = "Lấy thông tin chuỗi ngày học hiện tại của tôi")
    public ApiResponse<UserStreakResponse> getMyStreak() {
        return ApiResponse.ok(progressService.getMyStreak(), SuccessCode.GET_STREAK_SUCCESS);
    }

    @GetMapping("/activities")
    @Operation(summary = "Lấy dữ liệu nhật ký đóng góp học tập (Heatmap)")
    public ApiResponse<List<UserActivityResponse>> getMyActivities(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();
        LocalDate start = (startDate != null) ? startDate : end.minusYears(1);
        return ApiResponse.ok(progressService.getMyActivities(start, end), SuccessCode.GET_ACTIVITIES_SUCCESS);
    }
}
