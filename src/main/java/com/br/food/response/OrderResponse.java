package com.br.food.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.br.food.enums.Types.OrderChannel;
import com.br.food.enums.Types.OrderStatus;
import com.br.food.models.Order;

public class OrderResponse {

	private final Long id;
	private final String code;
	private final CustomerResponse customer;
	private final DiningTableResponse table;
	private final BigDecimal discountPercentage;
	private final BigDecimal totalAmount;
	private final LocalDateTime openedAt;
	private final LocalDateTime closedAt;
	private final OrderStatus status;
	private final OrderChannel channel;
	private final List<OrderItemResponse> items;

	public OrderResponse(Order order) {
		this.id = order.getId();
		this.code = order.getCode();
		this.customer = order.getCustomer() != null ? new CustomerResponse(order.getCustomer()) : null;
		this.table = order.getDiningTable() != null ? new DiningTableResponse(order.getDiningTable()) : null;
		this.discountPercentage = order.getDiscountPercentage();
		this.totalAmount = order.getTotalAmount();
		this.openedAt = order.getOpenedAt();
		this.closedAt = order.getClosedAt();
		this.status = order.getStatus();
		this.channel = order.getChannel();
		this.items = order.getItems().stream().map(OrderItemResponse::new).toList();
	}

	public Long getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public CustomerResponse getCustomer() {
		return customer;
	}

	public DiningTableResponse getTable() {
		return table;
	}

	public BigDecimal getDiscountPercentage() {
		return discountPercentage;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public LocalDateTime getOpenedAt() {
		return openedAt;
	}

	public LocalDateTime getClosedAt() {
		return closedAt;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public OrderChannel getChannel() {
		return channel;
	}

	public List<OrderItemResponse> getItems() {
		return items;
	}
}
