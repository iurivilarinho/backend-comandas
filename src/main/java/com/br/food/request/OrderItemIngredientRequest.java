package com.br.food.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Customized ingredient selection for one order item")
public class OrderItemIngredientRequest {

	@NotNull(message = "Ingredient product id is required.")
	private Long ingredientProductId;

	@NotNull(message = "Ingredient quantity is required.")
	@DecimalMin(value = "0.0", message = "Ingredient quantity cannot be negative.")
	private BigDecimal quantity;

	public Long getIngredientProductId() {
		return ingredientProductId;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}
}
