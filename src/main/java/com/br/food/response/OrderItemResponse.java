package com.br.food.response;

import java.time.LocalDateTime;

import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.models.OrderItem;

public class OrderItemResponse {

	private final Long id;
	private final ProductBasicResponse product;
	private final Integer quantity;
	private final String notes;
	private final LocalDateTime requestedAt;
	private final OrderItemStatus status;
	private final String declineReason;

	public OrderItemResponse(OrderItem item) {
		this.id = item.getId();
		this.product = item.getProduct() != null ? new ProductBasicResponse(item.getProduct()) : null;
		this.quantity = item.getQuantity();
		this.notes = item.getNotes();
		this.requestedAt = item.getRequestedAt();
		this.status = item.getStatus();
		this.declineReason = item.getDeclineReason();
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

	public String getNotes() {
		return notes;
	}

	public LocalDateTime getRequestedAt() {
		return requestedAt;
	}

	public OrderItemStatus getStatus() {
		return status;
	}

	public String getDeclineReason() {
		return declineReason;
	}
}
