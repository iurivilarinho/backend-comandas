package com.br.food.response;

import java.math.BigDecimal;

import com.br.food.models.Order;

public class OrderCheckoutResponse {

	private final Long orderId;
	private final BigDecimal totalAmount;
	private final BigDecimal paidAmount;
	private final BigDecimal remainingAmount;
	private final BigDecimal changeAmount;
	private final BigDecimal amountPerPerson;
	private final boolean closed;

	public OrderCheckoutResponse(
			Order order,
			BigDecimal remainingAmount,
			BigDecimal changeAmount,
			BigDecimal amountPerPerson,
			boolean closed) {
		this.orderId = order.getId();
		this.totalAmount = order.getTotalAmount();
		this.paidAmount = order.getPaidAmount();
		this.remainingAmount = remainingAmount;
		this.changeAmount = changeAmount;
		this.amountPerPerson = amountPerPerson;
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

	public BigDecimal getAmountPerPerson() {
		return amountPerPerson;
	}

	public boolean isClosed() {
		return closed;
	}
}
