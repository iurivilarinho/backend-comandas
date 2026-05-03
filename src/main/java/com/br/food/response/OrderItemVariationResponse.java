package com.br.food.response;

import java.math.BigDecimal;

import com.br.food.models.OrderItemVariation;

public class OrderItemVariationResponse {

	private final Long id;
	private final Long productVariationId;
	private final String variationName;
	private final BigDecimal priceDelta;
	private final String groupTitle;

	public OrderItemVariationResponse(OrderItemVariation variation) {
		this.id = variation.getId();
		this.productVariationId = variation.getProductVariationId();
		this.variationName = variation.getVariationName();
		this.priceDelta = variation.getPriceDelta();
		this.groupTitle = variation.getGroupTitle();
	}

	public Long getId() {
		return id;
	}

	public Long getProductVariationId() {
		return productVariationId;
	}

	public String getVariationName() {
		return variationName;
	}

	public BigDecimal getPriceDelta() {
		return priceDelta;
	}

	public String getGroupTitle() {
		return groupTitle;
	}
}
