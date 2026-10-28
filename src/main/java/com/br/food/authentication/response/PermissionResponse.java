package com.br.food.authentication.response;

import com.br.food.authentication.models.PermissionResource;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta completa de permissões (CRUD) de um role em um recurso.")
public record PermissionResponse(

		@Schema(description = "Identificador único do registro de permissão.", example = "100") Long id,

		@Schema(description = "Perfil vinculado a permissão") RoleBasicResponse role,

		@Schema(description = "Recurso vinculado a permissão") ResourceResponse resource,

		@Schema(description = "Permissão para criar.", example = "true") Boolean canCreate,

		@Schema(description = "Permissão para ler.", example = "true") Boolean canRead,

		@Schema(description = "Permissão para atualizar.", example = "true") Boolean canUpdate,

		@Schema(description = "Permissão para excluir.", example = "false") Boolean canDelete

) {
	public PermissionResponse(PermissionResource permission) {
		this(permission.getId(), new RoleBasicResponse(permission.getRole()),
				permission.getResource() != null ? new ResourceResponse(permission.getResource()) : null,
				permission.getCanCreate(), permission.getCanRead(), permission.getCanUpdate(),
				permission.getCanDelete());
	}
}
