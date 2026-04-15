package com.br.food.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RecipeItemRequest {

	@NotNull(message = "Ingredient product id is required.")
	private Long ingredientProductId;

	@NotNull(message = "Ingredient quantity is required.")
	@Positive(message = "Ingredient quantity must be greater than zero.")
	private BigDecimal quantity;

	public Long getIngredientProductId() {
		return ingredientProductId;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}
}
