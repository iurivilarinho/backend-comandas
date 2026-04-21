package com.br.food.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class FinancialSettingsRequest {

	@NotNull(message = "Service fee percent is required.")
	@DecimalMin(value = "0.0", message = "Service fee percent cannot be negative.")
	@DecimalMax(value = "100.0", message = "Service fee percent cannot exceed 100.")
	private BigDecimal serviceFeePercent;

	public BigDecimal getServiceFeePercent() {
		return serviceFeePercent;
	}
}
