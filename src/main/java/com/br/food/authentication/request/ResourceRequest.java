package com.br.food.authentication.request;

import com.br.food.authentication.enums.ResourceType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request para criação/atualização de recurso.")
public record ResourceRequest(

        @NotNull(message = "Tipo do recurso é obrigatório.")
        @Schema(description = "Tipo do recurso.", example = "SCREEN")
        ResourceType type,

        @NotBlank(message = "Referência do componente é obrigatória.")
        @Size(max = 150, message = "Referência do componente deve ter no máximo 150 caracteres.")
        @Schema(description = "Referência do componente no front-end.", example = "users.list.table")
        String componentReference,

        @NotBlank(message = "Título é obrigatório.")
        @Size(max = 150, message = "Título deve ter no máximo 150 caracteres.")
        @Schema(description = "Título do recurso.", example = "Listar usuários")
        String title,

        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres.")
        @Schema(description = "Descrição do recurso.", example = "Permite visualizar a listagem de usuários.")
        String description,

        @NotNull(message = "Status é obrigatório.")
        @Schema(description = "Status do recurso (ativo/inativo).", example = "true")
        Boolean active

) {
}
