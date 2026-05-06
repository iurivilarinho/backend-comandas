package com.br.food.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Customer payload")
public class CustomerRequest {

	@NotBlank(message = "Name is required.")
	@Size(max = 100, message = "Name must have at most 100 characters.")
	private String name;

	@NotBlank(message = "CPF is required.")
	@Size(min = 11, max = 11, message = "CPF must have exactly 11 digits.")
	@Pattern(regexp = "^[0-9]{11}$", message = "CPF must contain only 11 digits.")
	private String documentNumber;

	@NotBlank(message = "Phone is required.")
	@Size(min = 10, max = 15, message = "Phone must have between 10 and 15 characters.")
	private String phone;

	@Valid
	private AddressRequest address;

	public String getName() {
		return name;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public String getPhone() {
		return phone;
	}

	public AddressRequest getAddress() {
		return address;
	}
}
