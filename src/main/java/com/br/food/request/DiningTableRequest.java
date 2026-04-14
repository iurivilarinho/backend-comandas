package com.br.food.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dining table payload")
public class DiningTableRequest {

	@NotBlank(message = "Number is required.")
	@Size(max = 10, message = "Number must have at most 10 characters.")
	private String number;

	public String getNumber() {
		return number;
	}
}
