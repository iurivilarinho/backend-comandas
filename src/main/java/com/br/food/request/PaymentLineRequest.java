package com.br.food.request;

import java.math.BigDecimal;

import com.br.food.enums.Types.PaymentMethod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class PaymentLineRequest {

	@NotNull(message = "Payment method is required.")
	private PaymentMethod paymentMethod;

	@NotNull(message = "Payment amount is required.")
	@DecimalMin(value = "0.01", message = "Payment amount must be greater than zero.")
	private BigDecimal amount;

	@DecimalMin(value = "0.0", message = "Cash received cannot be negative.")
	private BigDecimal cashReceived;

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public BigDecimal getCashReceived() {
		return cashReceived;
	}
}
