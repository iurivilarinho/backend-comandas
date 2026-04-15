package com.br.food.authentication.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "GoogleLoginRequest", description = "Dados para autenticacao com Google via authorization code.")
public record GoogleLoginRequest(
		@NotBlank @Schema(description = "Authorization code retornado pelo Google Identity Services.", example = "4/0AbCdEfGhIj...") String code,
		@NotBlank @Schema(description = "Origin do frontend que iniciou o fluxo Google.", example = "http://localhost:5173") String redirectUri) {
}
