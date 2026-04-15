package com.br.food.response;

import java.math.BigDecimal;

import com.br.food.models.RecipeItem;

public class RecipeItemResponse {

	private final Long id;
	private final ProductBasicResponse ingredientProduct;
	private final BigDecimal quantity;

	public RecipeItemResponse(RecipeItem recipeItem) {
		this.id = recipeItem.getId();
		this.ingredientProduct = new ProductBasicResponse(recipeItem.getIngredientProduct());
		this.quantity = recipeItem.getQuantity();
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
}
