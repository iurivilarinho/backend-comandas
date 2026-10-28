package com.br.food.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Ativa ou desativa o acesso de um usuário")
public record UserActiveRequest(
		@Schema(description = "Verdadeiro para permitir o acesso", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "A situação é obrigatória.") Boolean active) {
}
