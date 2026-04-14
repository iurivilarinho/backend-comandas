package com.br.food.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Order item payload")
public class OrderItemRequest {

	@NotNull(message = "Product id is required.")
	private Long productId;

	@NotNull(message = "Quantity is required.")
	@Positive(message = "Quantity must be greater than zero.")
	private Integer quantity;

	@Size(max = 255, message = "Notes must have at most 255 characters.")
	private String notes;

	public Long getProductId() {
		return productId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public String getNotes() {
		return notes;
	}
}
