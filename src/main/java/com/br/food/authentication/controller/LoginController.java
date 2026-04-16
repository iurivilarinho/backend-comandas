package com.br.food.authentication.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.authentication.GoogleAuthenticationService;
import com.br.food.authentication.GoogleLoginRequest;
import com.br.food.authentication.LoginRequest;
import com.br.food.authentication.response.UserProfileResponse;
import com.br.food.authentication.token.RefreshTokenService;
import com.br.food.authentication.token.TokenService;
import com.br.food.models.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Tag(name = "Auth", description = "Endpoints de autenticacao e controle de acesso")
@RestController
@RequestMapping("/auth")
public class LoginController {

	private final AuthenticationManager manager;
	private final TokenService tokenService;
	private final RefreshTokenService refreshTokenService;
	private final GoogleAuthenticationService googleAuthenticationService;

	public LoginController(AuthenticationManager manager, TokenService tokenService,
			RefreshTokenService refreshTokenService, GoogleAuthenticationService googleAuthenticationService) {
		this.manager = manager;
		this.tokenService = tokenService;
		this.refreshTokenService = refreshTokenService;
		this.googleAuthenticationService = googleAuthenticationService;
	}

	@Operation(summary = "Login", description = "Realiza autenticacao via credenciais.")
	@PostMapping("/login")
	public ResponseEntity<UserProfileResponse> efetuarLogin(@RequestBody LoginRequest dados,
			HttpServletRequest request) {
		try {
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
					dados.login(), dados.password());
			var authentication = manager.authenticate(authenticationToken);
			User authenticatedUser = (User) authentication.getPrincipal();
			return buildAuthenticatedResponse(authenticatedUser, request);
		} catch (BadCredentialsException exception) {
			throw new AccessDeniedException("Senha incorreta! Tente novamente.");
		}
	}

	@Operation(summary = "Login com Google", description = "Realiza autenticacao via Google Identity Services usando authorization code.")
	@PostMapping("/google")
	public ResponseEntity<UserProfileResponse> efetuarLoginGoogle(@Valid @RequestBody GoogleLoginRequest request,
			HttpServletRequest httpRequest) {
		User authenticatedUser = googleAuthenticationService.authenticate(request);
		return buildAuthenticatedResponse(authenticatedUser, httpRequest);
	}

	@Operation(summary = "Renova o access token via refresh token (com rotacao)", description = "Le o cookie 'refresh_token', valida, revoga o antigo, emite um novo refresh e devolve um novo access curto no cookie 'token'.")
	@PostMapping("/refresh")
	public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
		String refreshRaw = readCookie(request, "refresh_token");
		if (refreshRaw == null) {
			throw new AccessDeniedException("refresh token ausente");
		}

		var active = refreshTokenService.mustFindActive(refreshRaw);
		User user = active.getUsuario();
		boolean secure = isSecureRequest(request);

		String newRefreshRaw = refreshTokenService.rotate(refreshRaw, request.getRemoteAddr(),
				request.getHeader("User-Agent"));
		refreshTokenService.writeRefreshCookie(response, newRefreshRaw, secure);

		String newAccess = tokenService.generateToken(user);

		Cookie access = new Cookie("token", newAccess);
		access.setHttpOnly(true);
		access.setSecure(secure);
		access.setPath("/");
		access.setMaxAge(300);
		access.setAttribute("SameSite", secure ? "None" : "Lax");
		response.addCookie(access);

		return ResponseEntity.ok().build();
	}

	private ResponseEntity<UserProfileResponse> buildAuthenticatedResponse(User authenticatedUser,
			HttpServletRequest request) {
		String tokenJWT = tokenService.generateToken(authenticatedUser);
		String refreshRaw = refreshTokenService.issue(authenticatedUser, request.getRemoteAddr(),
				request.getHeader("User-Agent"));

		HttpHeaders headers = buildAuthenticationHeaders(request, tokenJWT, refreshRaw);
		User user = tokenService.getUserFromToken(tokenJWT);
		return ResponseEntity.ok().headers(headers).body(new UserProfileResponse(user));
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

	private boolean isSecureRequest(HttpServletRequest request) {
		String forwardedProto = request.getHeader("X-Forwarded-Proto");
		return request.isSecure() || (forwardedProto != null && forwardedProto.equalsIgnoreCase("https"));
	}

	private String readCookie(HttpServletRequest request, String name) {
		if (request.getCookies() == null) {
			return null;
		}
		for (Cookie cookie : request.getCookies()) {
			if (name.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}

}

