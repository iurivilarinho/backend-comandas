package com.br.food.request;

import jakarta.validation.constraints.NotBlank;

public class CancelOrderRequest {

	@NotBlank(message = "Cancellation reason is required.")
	private String reason;

	public String getReason() {
		return reason;
	}
}
