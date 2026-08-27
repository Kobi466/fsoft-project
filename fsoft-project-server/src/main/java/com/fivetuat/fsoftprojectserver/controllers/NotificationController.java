package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.NotificationCreationRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.NotificationResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Notification Controller", description = "Quản lý thông báo người dùng")
public class NotificationController {

    NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Tạo mới thông báo", description = "Gửi một thông báo mới tới người dùng chỉ định.")
    public ApiResponse<NotificationResponse> createNotification(
            @RequestBody @Valid NotificationCreationRequest request
    ) {
        return ApiResponse.ok(notificationService.createNotification(request), SuccessCode.CREATE_NOTIFICATION_SUCCESS);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách thông báo của tôi", description = "Truy xuất danh sách thông báo của người dùng đang đăng nhập (sắp xếp mới nhất).")
    public ApiResponse<List<NotificationResponse>> getMyNotifications(Pageable pageable) {
        return ApiResponse.ok(notificationService.getMyNotifications(pageable), SuccessCode.GET_NOTIFICATIONS_SUCCESS);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Đếm số thông báo chưa đọc", description = "Trả về số lượng thông báo chưa đọc của người dùng hiện tại.")
    public ApiResponse<Long> getUnreadCount() {
        return ApiResponse.ok(notificationService.getUnreadCount(), SuccessCode.GET_UNREAD_NOTIFICATION_COUNT_SUCCESS);
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Đánh dấu thông báo đã đọc", description = "Cập nhật trạng thái thông báo theo ID thành đã đọc.")
    public ApiResponse<NotificationResponse> markAsRead(
            @Parameter(description = "ID thông báo") @PathVariable Long id
    ) {
        return ApiResponse.ok(notificationService.markAsRead(id), SuccessCode.MARK_NOTIFICATION_READ_SUCCESS);
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Đánh dấu tất cả thông báo đã đọc", description = "Chuyển tất cả thông báo chưa đọc của người dùng hiện tại sang trạng thái đã đọc.")
    public ApiResponse<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ApiResponse.ok(null, SuccessCode.MARK_NOTIFICATION_READ_SUCCESS);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa thông báo", description = "Xóa một thông báo theo ID.")
    public ApiResponse<Void> deleteNotification(
            @Parameter(description = "ID thông báo") @PathVariable Long id
    ) {
        notificationService.deleteNotification(id);
        return ApiResponse.ok(null, SuccessCode.DELETE_NOTIFICATION_SUCCESS);
    }
}
