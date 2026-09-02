package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminDashboardStatsResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
//@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Dashboard Controller", description = "Thống kê tổng quan hệ thống Admin Dashboard")
public class AdminDashboardController {

    AdminDashboardService adminDashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Lấy các chỉ số KPI thống kê tổng quan hệ thống")
    public ApiResponse<AdminDashboardStatsResponse> getDashboardStats() {
        return ApiResponse.ok(adminDashboardService.getDashboardStats(), SuccessCode.GET_DASHBOARD_STATS_SUCCESS);
    }
}
