package com.br.food.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductVariationRequest {

	private Long id;

	@NotBlank(message = "Variation name is required.")
	@Size(max = 100, message = "Variation name must have at most 100 characters.")
	private String name;

	@NotNull(message = "Variation price delta is required.")
	@DecimalMin(value = "0.0", message = "Variation price delta cannot be negative.")
	private BigDecimal priceDelta;

	private Boolean active;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public BigDecimal getPriceDelta() {
		return priceDelta;
	}

	public Boolean getActive() {
		return active;
	}

	public boolean getResolvedActive() {
		return !Boolean.FALSE.equals(active);
	}
}
