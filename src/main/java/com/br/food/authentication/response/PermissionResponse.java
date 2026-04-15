package com.br.food.authentication.response;

import com.br.food.authentication.models.PermissionResource;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta completa de permissÃµes (CRUD) de um role em um recurso.")
public record PermissionResponse(

		@Schema(description = "Identificador Ãºnico do registro de permissÃ£o.", example = "100") Long id,

		@Schema(description = "Perfil vinculado a permissÃ£o") RoleBasicResponse role,

		@Schema(description = "Recurso vinculado a permissÃ£o") ResourceResponse resource,

		@Schema(description = "PermissÃ£o para criar.", example = "true") Boolean canCreate,

		@Schema(description = "PermissÃ£o para ler.", example = "true") Boolean canRead,

		@Schema(description = "PermissÃ£o para atualizar.", example = "true") Boolean canUpdate,

		@Schema(description = "PermissÃ£o para excluir.", example = "false") Boolean canDelete

) {
	public PermissionResponse(PermissionResource permission) {
		this(permission.getId(), new RoleBasicResponse(permission.getRole()),
				permission.getResource() != null ? new ResourceResponse(permission.getResource()) : null,
				permission.getCanCreate(), permission.getCanRead(), permission.getCanUpdate(),
				permission.getCanDelete());
	}
}
