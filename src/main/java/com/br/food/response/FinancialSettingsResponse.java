package com.br.food.response;

import java.math.BigDecimal;

public class FinancialSettingsResponse {

	private final BigDecimal serviceFeePercent;

	public FinancialSettingsResponse(BigDecimal serviceFeePercent) {
		this.serviceFeePercent = serviceFeePercent;
	}

	public BigDecimal getServiceFeePercent() {
		return serviceFeePercent;
	}
}
