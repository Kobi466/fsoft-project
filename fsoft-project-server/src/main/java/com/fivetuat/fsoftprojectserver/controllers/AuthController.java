package com.fivetuat.fsoftprojectserver.controllers;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fivetuat.fsoftprojectserver.annotations.RateLimit;
import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.AuthenticatedRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.ForgotPasswordRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.IntrospectTokenRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.LogoutRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.RefreshTokenRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.ResetPasswordRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.UserCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AuthenticatedResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.IntrospectTokenResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.TokenResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.UserResponse;
import com.fivetuat.fsoftprojectserver.enums.RateLimitKeyType;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/auth")
@Tag(name = "Auth Controller", description = "Quản lý đăng ký, đăng nhập, bảo mật và xác thực người dùng (JWT)")
public class AuthController {
    AuthService authService;

    @Operation(
        summary = "Đăng ký tài khoản mới", 
        description = "Tạo một tài khoản người dùng mới vào hệ thống. Yêu cầu truyền lên email hợp lệ, mật khẩu (đủ độ mạnh) và họ tên người dùng. Trả về thông tin người dùng vừa được tạo nếu thành công."
    )
    @RateLimit(limit = 10, period = 60, keyType = RateLimitKeyType.IP, name = "auth_register")
    @PostMapping("/register")
    ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        return ApiResponse.ok(this.authService.createUser(userCreateRequest), SuccessCode.CREATED_USER_SUCCESS);
    }

    @Operation(
        summary = "Đăng nhập hệ thống", 
        description = "Xác thực người dùng dựa trên email và mật khẩu. Nếu hợp lệ, hệ thống sẽ trả về Access Token (dùng để gọi các API khác) và Refresh Token (dùng để cấp lại Access Token khi hết hạn)."
    )
    @RateLimit(limit = 10, period = 60, keyType = RateLimitKeyType.IP, name = "auth_login")
    @PostMapping("/login")
    ApiResponse<AuthenticatedResponse> authenticated(@Valid @RequestBody AuthenticatedRequest authenticatedRequest) {
        return ApiResponse.ok(this.authService.authenticated(authenticatedRequest), SuccessCode.AUTHENTICATED_SUCCESS);
    }

    @Operation(
        summary = "Làm mới Access Token", 
        description = "Sử dụng Refresh Token còn hiệu lực để xin cấp lại một Access Token mới mà không cần người dùng phải đăng nhập lại."
    )
    @PostMapping("/refresh-token")
    ApiResponse<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        return ApiResponse.ok(this.authService.refreshToken(refreshTokenRequest), SuccessCode.REFRESH_TOKEN_SUCCESS);
    }

    @Operation(
        summary = "Đăng xuất tài khoản", 
        description = "Hủy bỏ hiệu lực của Access Token và Refresh Token hiện tại, đồng thời ghi nhận người dùng đã đăng xuất khỏi hệ thống."
    )
    @PostMapping("/logout")
    ApiResponse<String> logout(@Valid @RequestBody LogoutRequest logoutRequest) {
        this.authService.logout(logoutRequest);
        return ApiResponse.ok(SuccessCode.LOGOUT_SUCCESS.getMessage(), SuccessCode.LOGOUT_SUCCESS);
    }

    @Operation(
        summary = "Yêu cầu khôi phục mật khẩu", 
        description = "Gửi một liên kết hoặc mã khôi phục mật khẩu đến email của người dùng nếu họ quên mật khẩu đăng nhập."
    )
    @PostMapping("/forgot-password")
    ApiResponse<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        this.authService.forgotPassword(request);
        return ApiResponse.ok(null, SuccessCode.FORGOT_PASSWORD_SUCCESS);
    }

    @Operation(
        summary = "Đặt lại mật khẩu mới", 
        description = "Sử dụng mã xác thực (OTP/Token) nhận được từ email để tiến hành đổi mật khẩu mới."
    )
    @PostMapping("/reset-password")
    ApiResponse<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        this.authService.resetPassword(request);
        return ApiResponse.ok(null, SuccessCode.RESET_PASSWORD_SUCCESS);
    }

    @Operation(
        summary = "Kiểm tra trạng thái Token (Introspect)", 
        description = "Kiểm tra xem một Access Token hoặc Refresh Token có còn hiệu lực hay không, đã bị thu hồi (logout) hay hết hạn chưa."
    )
    @PostMapping("/introspect")
    ApiResponse<IntrospectTokenResponse> logout(@Valid @RequestBody IntrospectTokenRequest request) {
        return ApiResponse.ok(this.authService.introspectToken(request), SuccessCode.INTROSPECT_TOKEN_SUCCESS);
    }
}
