package com.br.food.authentication.request;

import com.br.food.authentication.enums.ResourceType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request para criaÃ§Ã£o/atualizaÃ§Ã£o de recurso.")
public record ResourceRequest(

        @NotNull(message = "Tipo do recurso Ã© obrigatÃ³rio.")
        @Schema(description = "Tipo do recurso.", example = "SCREEN")
        ResourceType type,

        @NotBlank(message = "ReferÃªncia do componente Ã© obrigatÃ³ria.")
        @Size(max = 150, message = "ReferÃªncia do componente deve ter no mÃ¡ximo 150 caracteres.")
        @Schema(description = "ReferÃªncia do componente no front-end.", example = "users.list.table")
        String componentReference,

        @NotBlank(message = "TÃ­tulo Ã© obrigatÃ³rio.")
        @Size(max = 150, message = "TÃ­tulo deve ter no mÃ¡ximo 150 caracteres.")
        @Schema(description = "TÃ­tulo do recurso.", example = "Listar usuÃ¡rios")
        String title,

        @Size(max = 255, message = "DescriÃ§Ã£o deve ter no mÃ¡ximo 255 caracteres.")
        @Schema(description = "DescriÃ§Ã£o do recurso.", example = "Permite visualizar a listagem de usuÃ¡rios.")
        String description,

        @NotNull(message = "Status Ã© obrigatÃ³rio.")
        @Schema(description = "Status do recurso (ativo/inativo).", example = "true")
        Boolean active

) {
}
