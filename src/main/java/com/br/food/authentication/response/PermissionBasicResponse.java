package com.br.food.authentication.response;

import com.br.food.authentication.models.PermissionResource;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta bÃ¡sica de permissÃµes (para listagens).")
public record PermissionBasicResponse(

		@Schema(description = "Identificador do registro de permissÃ£o.", example = "100") Long id,

		@Schema(description = "Identificador do role.", example = "1") Long roleId,

		@Schema(description = "Identificador do recurso.", example = "10") Long resourceId

) {
	public PermissionBasicResponse(PermissionResource permission) {
		this(permission.getId(), permission.getRole() != null ? permission.getRole().getId() : null,
				permission.getResource() != null ? permission.getResource().getId() : null);
	}
}
