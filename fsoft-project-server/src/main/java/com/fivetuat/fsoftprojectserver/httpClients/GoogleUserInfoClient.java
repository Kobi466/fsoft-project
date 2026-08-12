package com.fivetuat.fsoftprojectserver.httpClients;

import com.fivetuat.fsoftprojectserver.dtos.response.GoogleInfoUserResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "https://www.googleapis.com")
public interface GoogleUserInfoClient {
	@GetExchange("/oauth2/v1/userinfo")
	GoogleInfoUserResponse getUserInfo(
			@RequestParam("alt") String alt,
			@RequestParam("access_token") String accessToken
	);
}
