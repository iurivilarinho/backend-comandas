package com.br.food.authentication.response;

import com.br.food.authentication.models.Role;

import io.swagger.v3.oas.annotations.media.Schema;

public class RoleBasicResponse {
	@Schema(description = "Identificador Ãºnico do perfil.", example = "1")
	private Long id;

	@Schema(description = "Nome do perfil.", example = "ROLE_ADMIN")
	private String name;

	@Schema(description = "Status do perfil (ativo/inativo).", example = "true")
	private Boolean active;

	public RoleBasicResponse(Role role) {
		this.id = role.getId();
		this.name = role.getName();
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

}
