package com.fivetuat.fsoftprojectserver.controllers;

import org.springframework.web.bind.annotation.*;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AuthenticatedResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "OAuth2 Controller", description = "Quản lý việc đăng nhập và liên kết tài khoản thông qua nền tảng thứ ba (Google)")
public class Oauth2Controller {
	AuthService authService;

    @Operation(
        summary = "Callback Đăng nhập qua Google (Google Sign-In)", 
        description = "Sau khi người dùng đồng ý cấp quyền trên cửa sổ đăng nhập Google, trình duyệt sẽ bị redirect về API này kèm theo tham số 'code'. Backend sẽ trao đổi code lấy thông tin Profile Google và sinh ra Access Token của hệ thống trả về cho Client."
    )
	@PostMapping("/callback")
	ApiResponse<AuthenticatedResponse> authenticatedUserGooglePost(
        @Parameter(description = "Mã xác thực (Authorization Code) trả về từ Google Server") @RequestParam("code") String code
    ) {
		return ApiResponse.ok(this.authService.authenticatedUserGoogle(code), SuccessCode.AUTHENTICATED_GOOGLE_SUCCESS);
	}
}
