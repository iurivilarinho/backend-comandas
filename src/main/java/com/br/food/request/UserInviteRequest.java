package com.br.food.request;

import com.br.food.enums.Types.StaffRole;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Convite para um novo usuário da equipe")
public record UserInviteRequest(
		@Schema(description = "Nome do usuário", example = "Maria Souza", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "O nome é obrigatório.") @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.") String name,
		@Schema(description = "E-mail, que também é o login", example = "maria@restaurante.com", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "O e-mail é obrigatório.") @Email(message = "E-mail inválido.") @Size(max = 100, message = "O e-mail deve ter no máximo 100 caracteres.") String email,
		@Schema(description = "Papel do usuário", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "O papel é obrigatório.") StaffRole role) {
}
