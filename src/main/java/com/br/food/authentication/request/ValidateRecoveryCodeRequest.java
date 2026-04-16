package com.br.food.authentication.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ValidateRecoveryCodeRequest", description = "Dados para validaÃ§Ã£o do cÃ³digo de recuperaÃ§Ã£o.")
public class ValidateRecoveryCodeRequest {

	@Schema(description = "E-mail do usuÃ¡rio.", example = "usuario@exemplo.com")
	private String email;

	@Schema(description = "CÃ³digo de recuperaÃ§Ã£o com 4 dÃ­gitos.", example = "1234")
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
