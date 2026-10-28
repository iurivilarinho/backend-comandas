package com.br.food.authentication.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request para criação/atualização de permissões (CRUD) de um role em um recurso.")
public record PermissionRequest(

        @NotNull(message = "Role é obrigatório.")
        @Schema(description = "Identificador do role.", example = "1")
        Long roleId,

        @NotNull(message = "Recurso é obrigatório.")
        @Schema(description = "Identificador do recurso.", example = "10")
        Long resourceId,

        @NotNull(message = "Permissão de criação é obrigatória.")
        @Schema(description = "Permissão para criar.", example = "true")
        Boolean canCreate,

        @NotNull(message = "Permissão de leitura é obrigatória.")
        @Schema(description = "Permissão para ler.", example = "true")
        Boolean canRead,

        @NotNull(message = "Permissão de atualização é obrigatória.")
        @Schema(description = "Permissão para atualizar.", example = "true")
        Boolean canUpdate,

        @NotNull(message = "Permissão de exclusão é obrigatória.")
        @Schema(description = "Permissão para excluir.", example = "false")
        Boolean canDelete

) {
}
