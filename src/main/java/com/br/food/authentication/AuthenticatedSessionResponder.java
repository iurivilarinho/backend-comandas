package com.br.food.authentication;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.br.food.authentication.response.UserProfileResponse;
import com.br.food.authentication.token.RefreshTokenService;
import com.br.food.authentication.token.TokenService;
import com.br.food.models.User;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Builds the response of a successful sign-in (access and refresh cookies, plus the bearer headers for the mobile
 * clients). Shared by the password sign-in and the Google sign-in (PLT-038), so both answer the same way.
 */
@Component
public class AuthenticatedSessionResponder {

	private final TokenService tokenService;
	private final RefreshTokenService refreshTokenService;

	public AuthenticatedSessionResponder(TokenService tokenService, RefreshTokenService refreshTokenService) {
		this.tokenService = tokenService;
		this.refreshTokenService = refreshTokenService;
	}

	public ResponseEntity<UserProfileResponse> respond(User authenticatedUser, HttpServletRequest request) {
		String tokenJWT = tokenService.generateToken(authenticatedUser);
		String refreshRaw = refreshTokenService.issue(authenticatedUser, request.getRemoteAddr(),
				request.getHeader("User-Agent"));

		HttpHeaders headers = buildAuthenticationHeaders(request, tokenJWT, refreshRaw);
		User user = tokenService.getUserFromToken(tokenJWT);
		return ResponseEntity.ok().headers(headers).body(new UserProfileResponse(user));
	}

	public boolean isSecureRequest(HttpServletRequest request) {
		String forwardedProto = request.getHeader("X-Forwarded-Proto");
		return request.isSecure() || (forwardedProto != null && forwardedProto.equalsIgnoreCase("https"));
	}

	private HttpHeaders buildAuthenticationHeaders(HttpServletRequest request, String tokenJWT, String refreshRaw) {
		HttpHeaders headers = new HttpHeaders();
		String userAgent = request.getHeader("User-Agent");
		boolean isMobile = userAgent != null && (userAgent.contains("Mobi") || userAgent.contains("okhttp")
				|| userAgent.contains("Android") || userAgent.contains("iPhone"));
		boolean secure = isSecureRequest(request);

		ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshRaw).httpOnly(true).secure(secure)
				.sameSite(secure ? "None" : "Lax").path("/").maxAge(Duration.ofDays(15)).build();
		headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

		if (isMobile) {
			headers.add("Authorization", "Bearer " + tokenJWT);
			headers.add("X-Refresh", refreshRaw);
		}

		ResponseCookie accessCookie = ResponseCookie.from("token", tokenJWT).httpOnly(true).secure(secure)
				.sameSite(secure ? "None" : "Lax").path("/").maxAge(Duration.ofMinutes(5)).build();
		headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
		return headers;
	}
}
