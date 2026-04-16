package com.br.food.response;

import java.math.BigDecimal;

public class FinancialBreakdownResponse {

	private final String label;
	private final BigDecimal amount;

	public FinancialBreakdownResponse(String label, BigDecimal amount) {
		this.label = label;
		this.amount = amount;
	}

	public String getLabel() {
		return label;
	}

	public BigDecimal getAmount() {
		return amount;
	}
}
