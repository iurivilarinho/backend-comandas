package com.br.food.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.models.OrderItem;

public class OrderItemResponse {

	private final Long id;
	private final ProductBasicResponse product;
	private final Integer quantity;
	private final BigDecimal unitPrice;
	private final String notes;
	private final LocalDateTime requestedAt;
	private final List<OrderItemVariationResponse> variations;
	private final OrderItemStatus status;
	private final String declineReason;
	private final String cancellationReason;
	private final List<OrderItemIngredientResponse> ingredients;

	public OrderItemResponse(OrderItem item) {
		this.id = item.getId();
		this.product = item.getProduct() != null ? new ProductBasicResponse(item.getProduct()) : null;
		this.quantity = item.getQuantity();
		this.unitPrice = item.getUnitPrice();
		this.notes = item.getNotes();
		this.requestedAt = item.getRequestedAt();
		this.variations = item.getVariations().stream()
				.sorted(Comparator.comparing(variation -> variation.getDisplayOrder() != null ? variation.getDisplayOrder() : 0))
				.map(OrderItemVariationResponse::new)
				.toList();
		this.status = item.getStatus();
		this.declineReason = item.getDeclineReason();
		this.cancellationReason = item.getCancellationReason();
		this.ingredients = item.getIngredients().stream().map(OrderItemIngredientResponse::new).toList();
	}

	public Long getId() {
		return id;
	}

	public ProductBasicResponse getProduct() {
		return product;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public String getNotes() {
		return notes;
	}

	public LocalDateTime getRequestedAt() {
		return requestedAt;
	}

	public List<OrderItemVariationResponse> getVariations() {
		return variations;
	}

	public OrderItemStatus getStatus() {
		return status;
	}

	public String getDeclineReason() {
		return declineReason;
	}

	public String getCancellationReason() {
		return cancellationReason;
	}

	public List<OrderItemIngredientResponse> getIngredients() {
		return ingredients;
	}
}
