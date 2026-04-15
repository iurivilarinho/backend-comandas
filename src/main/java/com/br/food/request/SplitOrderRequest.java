package com.br.food.request;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class SplitOrderRequest {

	@NotNull(message = "Destination table id is required.")
	private Long destinationTableId;

	@NotEmpty(message = "Order item ids are required.")
	private List<Long> orderItemIds = new ArrayList<>();

	public Long getDestinationTableId() {
		return destinationTableId;
	}

	public List<Long> getOrderItemIds() {
		return orderItemIds;
	}
}
