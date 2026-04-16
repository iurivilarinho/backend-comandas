package com.br.food.authentication;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginRequest", description = "Login request payload.")
public record LoginRequest(

		@Schema(description = "Login/username.", example = "admin") String login,

		@Schema(description = "Raw password.", example = "admin123") String password) {
	public UsernamePasswordAuthenticationToken toAuthenticationToken() {
		return new UsernamePasswordAuthenticationToken(login, password);
	}
}

