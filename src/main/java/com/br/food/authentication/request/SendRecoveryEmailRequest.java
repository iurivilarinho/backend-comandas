package com.br.food.authentication.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SendRecoveryEmailRequest", description = "Dados para solicitaÃ§Ã£o de envio de cÃ³digo de recuperaÃ§Ã£o.")
public class SendRecoveryEmailRequest {
	@Schema(description = "E-mail do usuÃ¡rio que solicita recuperaÃ§Ã£o de senha.", example = "usuario@exemplo.com")
	private String email;

	public SendRecoveryEmailRequest() {
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
