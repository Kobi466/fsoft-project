package com.fivetuat.fsoftprojectserver.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ExchangeTokenResponse {
	@JsonProperty("access_token")
	String accessToken;
	@JsonProperty("expires_in")
	Long expiresIn;
	@JsonProperty("refresh_token")
	String refreshToken;
	@JsonProperty("refresh_token_expires_in")
	Long refreshTokenExpiresIn;
	@JsonProperty("scope")
	String scope;
	@JsonProperty("token_type")
	String tokenType;
}
