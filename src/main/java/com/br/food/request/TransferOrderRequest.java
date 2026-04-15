package com.br.food.request;

import jakarta.validation.constraints.NotBlank;

public class TransferOrderRequest {

	@NotBlank(message = "Target table number is required.")
	private String targetTableNumber;

	public String getTargetTableNumber() {
		return targetTableNumber;
	}
}
