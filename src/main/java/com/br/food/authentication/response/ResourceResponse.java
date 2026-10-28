package com.br.food.authentication.response;

import com.br.food.authentication.enums.ResourceType;
import com.br.food.authentication.models.Resource;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta completa do recurso.")
public record ResourceResponse(

		@Schema(description = "Identificador único do recurso.", example = "1") Long id,

		@Schema(description = "Tipo do recurso.", example = "SCREEN") ResourceType type,

		@Schema(description = "Referência do componente no front-end.", example = "users.list.table") String componentReference,

		@Schema(description = "Título do recurso.", example = "Listar usuários") String title,

		@Schema(description = "Descrição do recurso.", example = "Permite visualizar a listagem de usuários.") String description,

		@Schema(description = "Status do recurso (ativo/inativo).", example = "true") Boolean active

) {
	public ResourceResponse(Resource resource) {
		this(resource.getId(), resource.getType(), resource.getComponentReference(), resource.getTitle(),
				resource.getDescription(), resource.getActive());
	}
}
