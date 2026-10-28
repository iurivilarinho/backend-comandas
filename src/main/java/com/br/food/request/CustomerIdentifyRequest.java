package com.br.food.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para o cliente do cardápio digital recuperar o próprio cadastro em outro aparelho")
public class CustomerIdentifyRequest {

	@Schema(description = "Telefone cadastrado, com ou sem mascara.", example = "31999998888", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Phone is required.")
	private String phone;

	@Schema(description = "CPF cadastrado, com ou sem mascara.", example = "12345678901", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Document number is required.")
	private String documentNumber;

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}
}
