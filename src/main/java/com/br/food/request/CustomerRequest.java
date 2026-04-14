package com.br.food.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Customer payload")
public class CustomerRequest {

	@NotBlank(message = "Name is required.")
	@Size(max = 100, message = "Name must have at most 100 characters.")
	private String name;

	@NotBlank(message = "Document number is required.")
	@Size(min = 11, max = 14, message = "Document number must have between 11 and 14 characters.")
	private String documentNumber;

	@NotBlank(message = "Phone is required.")
	@Size(min = 10, max = 15, message = "Phone must have between 10 and 15 characters.")
	private String phone;

	public String getName() {
		return name;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public String getPhone() {
		return phone;
	}
}
