package com.fivetuat.fsoftprojectserver.httpClients;

import com.fivetuat.fsoftprojectserver.dtos.response.ExchangeTokenResponse;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(url = "https://oauth2.googleapis.com")
public interface GoogleOauth2Client {
	@PostExchange(value = "/token", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	ExchangeTokenResponse exchangeToken(@RequestBody MultiValueMap<String, String> formData);
}
