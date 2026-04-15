package com.br.food.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.br.food.enums.Types.PaymentMethod;
import com.br.food.models.OrderPayment;

public class OrderPaymentResponse {

	private final Long id;
	private final PaymentMethod paymentMethod;
	private final BigDecimal amount;
	private final BigDecimal cashReceived;
	private final String recordedBy;
	private final LocalDateTime recordedAt;

	public OrderPaymentResponse(OrderPayment orderPayment) {
		this.id = orderPayment.getId();
		this.paymentMethod = orderPayment.getPaymentMethod();
		this.amount = orderPayment.getAmount();
		this.cashReceived = orderPayment.getCashReceived();
		this.recordedBy = orderPayment.getRecordedBy();
		this.recordedAt = orderPayment.getRecordedAt();
	}

	public Long getId() {
		return id;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public BigDecimal getCashReceived() {
		return cashReceived;
	}

	public String getRecordedBy() {
		return recordedBy;
	}

	public LocalDateTime getRecordedAt() {
		return recordedAt;
	}
}
