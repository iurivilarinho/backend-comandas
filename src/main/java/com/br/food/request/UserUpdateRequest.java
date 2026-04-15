package com.br.food.request;

import java.util.HashSet;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "UserUpdateRequest", description = "Dados para atualização de usuário.")
public class UserUpdateRequest {

	@Schema(description = "Login do usuário.", example = "samuel.oliveira", maxLength = 30, requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "O campo 'Login' não pode ser nulo.")
	@Size(max = 30, message = "O valor máximo de caracteres para o campo 'login' é de 30 caracteres.")
	private String login;

	@Schema(description = "Nome completo do usuário.", example = "Samuel Rocha Oliveira", maxLength = 40, requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "O campo 'Nome' não pode ser nulo.")
	@Size(max = 40, message = "O valor máximo de caracteres para o campo 'Nome' é de 40 caracteres.")
	private String name;

	@Schema(description = "CPF do usuário (somente números).", example = "12345678901", maxLength = 11, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	@Size(max = 11, message = "O valor máximo de caracteres para o campo 'CPF' é de 11 caracteres.")
	private String cpf;

	@Schema(description = "Telefone corporativo do usuário.", example = "31999999999", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	private String cellphoneCorporate;

	@Schema(description = "E-mail do usuário.", example = "samuel.oliveira@empresa.com.br", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	private String email;

	@Schema(description = "Identificador do cargo do usuário.", example = "10", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	private Long jobPositionId;

	@Schema(description = "Lista de identificadores das filiais associadas ao usuário.", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	private Set<Long> companyBranchIds = new HashSet<>();

	public String getLogin() {
		return login;
	}

	public String getName() {
		return name;
	}

	public String getCpf() {
		return cpf;
	}

	public String getCellphoneCorporate() {
		return cellphoneCorporate;
	}

	public String getEmail() {
		return email;
	}

	public Long getJobPositionId() {
		return jobPositionId;
	}

	public Set<Long> getCompanyBranchIds() {
		return companyBranchIds;
	}

}
