package com.br.food.authentication.response;

import com.br.food.authentication.enums.ResourceType;
import com.br.food.authentication.models.Resource;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta completa do recurso.")
public record ResourceResponse(

		@Schema(description = "Identificador Ãºnico do recurso.", example = "1") Long id,

		@Schema(description = "Tipo do recurso.", example = "SCREEN") ResourceType type,

		@Schema(description = "ReferÃªncia do componente no front-end.", example = "users.list.table") String componentReference,

		@Schema(description = "TÃ­tulo do recurso.", example = "Listar usuÃ¡rios") String title,

		@Schema(description = "DescriÃ§Ã£o do recurso.", example = "Permite visualizar a listagem de usuÃ¡rios.") String description,

		@Schema(description = "Status do recurso (ativo/inativo).", example = "true") Boolean active

) {
	public ResourceResponse(Resource resource) {
		this(resource.getId(), resource.getType(), resource.getComponentReference(), resource.getTitle(),
				resource.getDescription(), resource.getActive());
	}
}
