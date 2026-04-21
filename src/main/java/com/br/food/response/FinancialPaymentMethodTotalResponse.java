package com.br.food.response;

import java.math.BigDecimal;

import com.br.food.enums.Types.PaymentMethod;
import com.br.food.util.FinancialLabelUtils;

public class FinancialPaymentMethodTotalResponse {

	private final PaymentMethod paymentMethod;
	private final String paymentMethodLabel;
	private final BigDecimal amount;

	public FinancialPaymentMethodTotalResponse(PaymentMethod paymentMethod, BigDecimal amount) {
		this.paymentMethod = paymentMethod;
		this.paymentMethodLabel = FinancialLabelUtils.paymentMethod(paymentMethod);
		this.amount = amount;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public String getPaymentMethodLabel() {
		return paymentMethodLabel;
	}

	public BigDecimal getAmount() {
		return amount;
	}
}
