package com.br.food.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "UserRequest", description = "Dados para criação/atualização de usuário.")
public class UserRequest {

	@Schema(description = "Login do usuário.", example = "samuel.oliveira", maxLength = 30)
	@NotNull(message = "O campo 'Login' não pode ser nulo.")
	@Size(max = 30, message = "O valor máximo de caracteres para o campo 'login' é de 30 caracteres.")
	private String login;

	@Schema(description = "Nome completo do usuário.", example = "Samuel Rocha Oliveira", maxLength = 40)
	@NotNull(message = "O campo 'Nome' não pode ser nulo.")
	@Size(max = 40, message = "O valor máximo de caracteres para o campo 'Nome' é de 40 caracteres.")
	private String name;

	@Schema(description = "Senha do usuário. Deve conter letra maiúscula, minúscula, número e caractere especial.", example = "Senha@123")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#\\-])[A-Za-z\\d@$!%*?&#\\-]{8,}$", message = "A senha deve conter pelo menos uma letra maiúscula, uma letra minúscula, um número e um caractere especial.")
	private String password;

	@Schema(description = "CPF do usuário (somente números).", example = "12345678901", maxLength = 11)
	@Size(max = 11, message = "O valor máximo de caracteres para o campo 'CPF' é de 11 caracteres.")
	private String cpf;

	@Schema(description = "E-mail do usuário.", example = "samuel.oliveira@empresa.com.br")
	private String email;

	public String getLogin() {
		return login;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public String getCpf() {
		return cpf;
	}

	public String getEmail() {
		return email;
	}

}
