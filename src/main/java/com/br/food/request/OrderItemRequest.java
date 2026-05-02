package com.br.food.request;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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

	private Long promotionId;

	private Long productVariationId;

	@Size(max = 255, message = "Notes must have at most 255 characters.")
	private String notes;

	@Valid
	private List<OrderItemIngredientRequest> ingredients = new ArrayList<>();

	public Long getProductId() {
		return productId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public Long getPromotionId() {
		return promotionId;
	}

	public Long getProductVariationId() {
		return productVariationId;
	}

	public String getNotes() {
		return notes;
	}

	public List<OrderItemIngredientRequest> getIngredients() {
		return ingredients;
	}
}
