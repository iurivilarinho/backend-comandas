package com.br.food.response;

import java.math.BigDecimal;

import com.br.food.models.Order;

public class OrderCheckoutResponse {

	private final Long orderId;
	private final BigDecimal totalAmount;
	private final BigDecimal paidAmount;
	private final BigDecimal remainingAmount;
	private final BigDecimal changeAmount;
	private final boolean closed;

	public OrderCheckoutResponse(
			Order order,
			BigDecimal remainingAmount,
			BigDecimal changeAmount,
			boolean closed) {
		this.orderId = order.getId();
		this.totalAmount = order.getTotalAmount();
		this.paidAmount = order.getPaidAmount();
		this.remainingAmount = remainingAmount;
		this.changeAmount = changeAmount;
		this.closed = closed;
	}

	public Long getOrderId() {
		return orderId;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public BigDecimal getPaidAmount() {
		return paidAmount;
	}

	public BigDecimal getRemainingAmount() {
		return remainingAmount;
	}

	public BigDecimal getChangeAmount() {
		return changeAmount;
	}

	public boolean isClosed() {
		return closed;
	}
}
