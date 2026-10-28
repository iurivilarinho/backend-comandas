package com.br.food.authentication.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SendRecoveryEmailRequest", description = "Dados para solicitação de envio de código de recuperação.")
public class SendRecoveryEmailRequest {
	@Schema(description = "E-mail do usuário que solicita recuperação de senha.", example = "usuario@exemplo.com")
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
