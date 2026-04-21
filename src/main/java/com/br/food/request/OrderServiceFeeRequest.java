package com.br.food.request;

import jakarta.validation.constraints.NotNull;

public class OrderServiceFeeRequest {

	@NotNull(message = "Apply service fee flag is required.")
	private Boolean applyServiceFee;

	public Boolean getApplyServiceFee() {
		return applyServiceFee;
	}
}
