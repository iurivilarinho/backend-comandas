package com.br.food.response;

import java.math.BigDecimal;

import com.br.food.enums.Types.PaymentMethod;

public class FinancialPaymentMethodTotalResponse {

	private final PaymentMethod paymentMethod;
	private final BigDecimal amount;

	public FinancialPaymentMethodTotalResponse(PaymentMethod paymentMethod, BigDecimal amount) {
		this.paymentMethod = paymentMethod;
		this.amount = amount;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public BigDecimal getAmount() {
		return amount;
	}
}
