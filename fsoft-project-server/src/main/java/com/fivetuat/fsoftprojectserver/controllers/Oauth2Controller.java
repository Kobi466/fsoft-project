package com.fivetuat.fsoftprojectserver.controllers;

import org.springframework.web.bind.annotation.*;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AuthenticatedResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.AuthService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Oauth2Controller {
	AuthService authService;

	@PostMapping("/callback")
	ApiResponse<AuthenticatedResponse> authenticatedUserGooglePost(@RequestParam("code") String code) {
		return ApiResponse.ok(this.authService.authenticatedUserGoogle(code), SuccessCode.AUTHENTICATED_GOOGLE_SUCCESS);
	}
}
