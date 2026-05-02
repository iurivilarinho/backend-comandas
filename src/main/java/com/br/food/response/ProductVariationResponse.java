package com.br.food.response;

import java.math.BigDecimal;

import com.br.food.models.ProductVariation;

public class ProductVariationResponse {

	private final Long id;
	private final String name;
	private final BigDecimal priceDelta;
	private final Boolean active;
	private final Integer displayOrder;

	public ProductVariationResponse(ProductVariation variation) {
		this.id = variation.getId();
		this.name = variation.getName();
		this.priceDelta = variation.getPriceDelta();
		this.active = variation.getActive();
		this.displayOrder = variation.getDisplayOrder();
	}

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

	public Integer getDisplayOrder() {
		return displayOrder;
	}
}
