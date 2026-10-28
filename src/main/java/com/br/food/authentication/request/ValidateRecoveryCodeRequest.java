package com.br.food.authentication.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ValidateRecoveryCodeRequest", description = "Dados para validação do código de recuperação.")
public class ValidateRecoveryCodeRequest {

	@Schema(description = "E-mail do usuário.", example = "usuario@exemplo.com")
	private String email;

	@Schema(description = "Código de recuperação com 4 dígitos.", example = "1234")
	private String code;

	public ValidateRecoveryCodeRequest() {
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
