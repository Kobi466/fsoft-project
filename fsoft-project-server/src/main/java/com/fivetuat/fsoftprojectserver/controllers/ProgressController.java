package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.UserActivityResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.UserStreakResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "Progress Controller", description = "Quản lý tiến độ học tập, chuỗi ngày liên tiếp (Streak) và đóng góp (Heatmap) của người dùng")
public class ProgressController {

    ProgressService progressService;

    @GetMapping("/streak")
    @Operation(
        summary = "Kiểm tra chuỗi ngày học tập (Streak)", 
        description = "API tính toán và trả về chuỗi số ngày liên tiếp người dùng có hoạt động ôn tập thẻ bài (Streak hiện tại, Streak dài nhất, đã học hôm nay chưa)."
    )
    public ApiResponse<UserStreakResponse> getMyStreak() {
        return ApiResponse.ok(progressService.getMyStreak(), SuccessCode.GET_STREAK_SUCCESS);
    }

    @GetMapping("/activities")
    @Operation(
        summary = "Vẽ biểu đồ Nhật ký đóng góp (Heatmap)", 
        description = "Lấy dữ liệu tần suất học tập theo từng ngày của người dùng (tương tự như GitHub Contribution Graph). Trả về danh sách ngày và số lượng thẻ đã ôn trong khoảng thời gian chỉ định."
    )
    public ApiResponse<List<UserActivityResponse>> getMyActivities(
            @Parameter(description = "Ngày bắt đầu lấy dữ liệu (Mặc định: 1 năm trước)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Ngày kết thúc lấy dữ liệu (Mặc định: Ngày hôm nay)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();
        LocalDate start = (startDate != null) ? startDate : end.minusYears(1);
        return ApiResponse.ok(progressService.getMyActivities(start, end), SuccessCode.GET_ACTIVITIES_SUCCESS);
    }
}
