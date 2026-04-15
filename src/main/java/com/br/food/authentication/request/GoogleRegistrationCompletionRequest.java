package com.br.food.authentication.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "GoogleRegistrationCompletionRequest", description = "Dados obrigatorios para concluir o cadastro iniciado via Google.")
public record GoogleRegistrationCompletionRequest(
		@NotBlank @Size(max = 40) @Schema(description = "Nome completo do usuario.", example = "Samuel Rocha Oliveira") String name,
		@NotBlank @Size(max = 30) @Schema(description = "Login do usuario.", example = "samuel.oliveira") String login,
		@NotBlank @Size(max = 11) @Schema(description = "CPF do usuario (somente numeros).", example = "12345678901") String cpf,
		@Schema(description = "Telefone corporativo do usuario.", example = "31999999999") String cellphoneCorporate) {
}
