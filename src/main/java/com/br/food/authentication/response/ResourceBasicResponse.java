package com.br.food.authentication.response;

import com.br.food.authentication.models.Resource;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta bÃ¡sica do recurso (para listagens/combos).")
public record ResourceBasicResponse(

		@Schema(description = "Identificador Ãºnico do recurso.", example = "1") Long id,

		@Schema(description = "TÃ­tulo do recurso.", example = "Listar usuÃ¡rios") String title,

		@Schema(description = "Status do recurso (ativo/inativo).", example = "true") Boolean active,

		@Schema(description = "DescriÃ§Ã£o do recurso.", example = "Permite visualizar a listagem de usuÃ¡rios.") String componentReference,

		@Schema(description = "Tipo do recurso.") String type,

		@Schema(description = "DescriÃ§Ã£o do recurso.", example = "Permite visualizar a listagem de usuÃ¡rios.") String description

) {
	public ResourceBasicResponse(Resource resource) {
		this(resource.getId(), resource.getTitle(), resource.getActive(), resource.getComponentReference(),
				resource.getType().getDescription(), resource.getDescription());
	}
}
