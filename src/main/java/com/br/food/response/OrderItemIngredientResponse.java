package com.br.food.response;

import java.math.BigDecimal;

import com.br.food.models.OrderItemIngredient;

public class OrderItemIngredientResponse {

	private final Long id;
	private final ProductBasicResponse ingredientProduct;
	private final BigDecimal quantity;
	private final BigDecimal baseQuantity;
	private final BigDecimal additionalQuantity;

	public OrderItemIngredientResponse(OrderItemIngredient ingredient) {
		this.id = ingredient.getId();
		this.ingredientProduct = new ProductBasicResponse(ingredient.getIngredientProduct());
		this.quantity = ingredient.getQuantity();
		this.baseQuantity = ingredient.getBaseQuantity();
		this.additionalQuantity = ingredient.getQuantity().subtract(ingredient.getBaseQuantity()).max(BigDecimal.ZERO);
	}

	public Long getId() {
		return id;
	}

	public ProductBasicResponse getIngredientProduct() {
		return ingredientProduct;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public BigDecimal getBaseQuantity() {
		return baseQuantity;
	}

	public BigDecimal getAdditionalQuantity() {
		return additionalQuantity;
	}
}
