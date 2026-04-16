package com.br.food.authentication.response;

import com.br.food.authentication.models.Role;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Retorno de perfil (role).")
public class RoleResponse {

	@Schema(description = "Identificador Ãºnico do perfil.", example = "1")
	private Long id;

	@Schema(description = "Nome do perfil.", example = "ROLE_ADMIN")
	private String name;

	@Schema(description = "DecriÃ§Ã£o do perfil.", example = "ROLE_ADMIN")
	private String description;

	@Schema(description = "Status do perfil (ativo/inativo).", example = "true")
	private Boolean active;

	public RoleResponse(Role role) {
		this.id = role.getId();
		this.name = role.getName();
		this.description = role.getDescription();
		this.active = role.getActive();
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Boolean getActive() {
		return active;
	}

	public String getDescription() {
		return description;
	}

}
