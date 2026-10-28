package com.br.food.request;

import com.br.food.enums.Types.StaffRole;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Novo papel de um usuário da equipe")
public record UserRoleRequest(
		@Schema(description = "Papel do usuário", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "O papel é obrigatório.") StaffRole role) {
}
