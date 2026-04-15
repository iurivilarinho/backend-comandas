package com.br.food.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RoleRequest {

	@NotBlank(message = "O campo nome não pode ser vazio.")
	@Size(max = 60, message = "O nome do perfil deve ter no máximo 60 caracteres.")
	private String name;

	@NotBlank(message = "O campo descrição não pode ser vazio.")
	@Size(max = 500, message = "A descrição do perfil deve ter no máximo 500 caracteres.")
	private String description;

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}
