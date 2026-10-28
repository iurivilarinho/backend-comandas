package com.br.food.authentication.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "GoogleRegistrationCompletionRequest", description = "Dados obrigatórios para concluir o cadastro iniciado via Google.")
public record GoogleRegistrationCompletionRequest(
		@NotBlank @Size(max = 40) @Schema(description = "Nome completo do usuário.", example = "Samuel Rocha Oliveira") String name,
		@NotBlank @Size(max = 30) @Schema(description = "Login do usuário.", example = "samuel.oliveira") String login,
		@NotBlank @Size(max = 11) @Schema(description = "CPF do usuário (somente números).", example = "12345678901") String cpf,
		@Schema(description = "Telefone corporativo do usuário.", example = "31999999999") String cellphoneCorporate) {
}
