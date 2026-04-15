package com.br.food.request;

import jakarta.validation.constraints.NotBlank;

public class KitchenDeclineRequest {

	@NotBlank(message = "Decline reason is required.")
	private String reason;

	public String getReason() {
		return reason;
	}
}
