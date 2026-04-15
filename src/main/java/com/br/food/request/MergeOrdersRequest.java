package com.br.food.request;

import jakarta.validation.constraints.NotNull;

public class MergeOrdersRequest {

	@NotNull(message = "Source order id is required.")
	private Long sourceOrderId;

	public Long getSourceOrderId() {
		return sourceOrderId;
	}
}
