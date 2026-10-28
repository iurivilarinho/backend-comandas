package com.br.food.authentication.response;

import com.br.food.authentication.models.Resource;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta básica do recurso (para listagens/combos).")
public record ResourceBasicResponse(

		@Schema(description = "Identificador único do recurso.", example = "1") Long id,

		@Schema(description = "Título do recurso.", example = "Listar usuários") String title,

		@Schema(description = "Status do recurso (ativo/inativo).", example = "true") Boolean active,

		@Schema(description = "Descrição do recurso.", example = "Permite visualizar a listagem de usuários.") String componentReference,

		@Schema(description = "Tipo do recurso.") String type,

		@Schema(description = "Descrição do recurso.", example = "Permite visualizar a listagem de usuários.") String description

) {
	public ResourceBasicResponse(Resource resource) {
		this(resource.getId(), resource.getTitle(), resource.getActive(), resource.getComponentReference(),
				resource.getType().getDescription(), resource.getDescription());
	}
}
