package com.br.food.authentication.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request para criaÃ§Ã£o/atualizaÃ§Ã£o de permissÃµes (CRUD) de um role em um recurso.")
public record PermissionRequest(

        @NotNull(message = "Role Ã© obrigatÃ³rio.")
        @Schema(description = "Identificador do role.", example = "1")
        Long roleId,

        @NotNull(message = "Recurso Ã© obrigatÃ³rio.")
        @Schema(description = "Identificador do recurso.", example = "10")
        Long resourceId,

        @NotNull(message = "PermissÃ£o de criaÃ§Ã£o Ã© obrigatÃ³ria.")
        @Schema(description = "PermissÃ£o para criar.", example = "true")
        Boolean canCreate,

        @NotNull(message = "PermissÃ£o de leitura Ã© obrigatÃ³ria.")
        @Schema(description = "PermissÃ£o para ler.", example = "true")
        Boolean canRead,

        @NotNull(message = "PermissÃ£o de atualizaÃ§Ã£o Ã© obrigatÃ³ria.")
        @Schema(description = "PermissÃ£o para atualizar.", example = "true")
        Boolean canUpdate,

        @NotNull(message = "PermissÃ£o de exclusÃ£o Ã© obrigatÃ³ria.")
        @Schema(description = "PermissÃ£o para excluir.", example = "false")
        Boolean canDelete

) {
}
