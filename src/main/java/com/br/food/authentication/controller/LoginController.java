package com.br.food.authentication.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.authentication.AuthenticatedSessionResponder;
import com.br.food.authentication.LoginRequest;
import com.br.food.authentication.response.UserProfileResponse;
import com.br.food.authentication.models.RefreshToken;
import com.br.food.authentication.token.RefreshTokenService;
import com.br.food.authentication.token.TokenService;
import com.br.food.models.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "Auth", description = "Endpoints de autenticacao e controle de acesso")
@RestController
@RequestMapping("/auth")
public class LoginController {

	private final AuthenticationManager manager;
	private final TokenService tokenService;
	private final RefreshTokenService refreshTokenService;
	private final AuthenticatedSessionResponder sessionResponder;

	public LoginController(AuthenticationManager manager, TokenService tokenService,
			RefreshTokenService refreshTokenService, AuthenticatedSessionResponder sessionResponder) {
		this.manager = manager;
		this.tokenService = tokenService;
		this.refreshTokenService = refreshTokenService;
		this.sessionResponder = sessionResponder;
	}

	@Operation(summary = "Login", description = "Realiza autenticacao via credenciais.")
	@PostMapping("/login")
	public ResponseEntity<UserProfileResponse> efetuarLogin(@RequestBody LoginRequest dados,
			HttpServletRequest request) {
		try {
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
					dados.login(), dados.password());
			Authentication authentication = manager.authenticate(authenticationToken);
			User authenticatedUser = (User) authentication.getPrincipal();
			return sessionResponder.respond(authenticatedUser, request);
		} catch (BadCredentialsException exception) {
			throw new AccessDeniedException("Senha incorreta! Tente novamente.");
		}
	}

	@Operation(summary = "Renova o access token via refresh token (com rotacao)", description = "Le o cookie 'refresh_token', valida, revoga o antigo, emite um novo refresh e devolve um novo access curto no cookie 'token'.")
	@PostMapping("/refresh")
	public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
		String refreshRaw = readCookie(request, "refresh_token");
		if (refreshRaw == null) {
			throw new AccessDeniedException("refresh token ausente");
		}

		RefreshToken active = refreshTokenService.mustFindActive(refreshRaw);
		User user = active.getUsuario();
		boolean secure = sessionResponder.isSecureRequest(request);

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
