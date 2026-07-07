package com.fivetuat.fsoftprojectserver.utils;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;
import java.util.UUID;

public final class SecurityUtils {

	private SecurityUtils() {
		// Prevent instantiation
	}

	/**
	 * Lấy đối tượng JWT hiện tại của user đang đăng nhập.
	 */
	public static Optional<Jwt> getCurrentUserJwt() {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		return Optional.ofNullable(securityContext.getAuthentication())
				.filter(authentication -> authentication instanceof JwtAuthenticationToken)
				.map(authentication -> (JwtAuthenticationToken) authentication)
				.map(JwtAuthenticationToken::getToken);
	}

	/**
	 * Lấy Username (hoặc User ID tùy theo subject cấu hình trong token)
	 */
	public static Optional<UUID> getCurrentUserLogin() {
		return getCurrentUserJwt().map(jwt -> UUID.fromString(jwt.getSubject()));
	}

	/**
	 * Lấy một Claim cụ thể từ token dưới dạng Object (Ví dụ: "email", "scope"...)
	 */
	public static Optional<Object> getClaim(String claimName) {
		return getCurrentUserJwt().map(jwt -> jwt.getClaim(claimName));
	}

	/**
	 * Lấy một Claim cụ thể dưới dạng String
	 */
	public static Optional<String> getClaimAsString(String claimName) {
		return getCurrentUserJwt().map(jwt -> jwt.getClaimAsString(claimName));
	}

	/**
	 * Kiểm tra xem user hiện tại có sở hữu một Quyền (Authority/Role) cụ thể hay không.
	 */
	public static boolean isCurrentUserInRole(String authority) {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		return Optional.ofNullable(securityContext.getAuthentication())
				.map(authentication -> authentication.getAuthorities().stream()
						.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority)))
				.orElse(false);
	}
}
