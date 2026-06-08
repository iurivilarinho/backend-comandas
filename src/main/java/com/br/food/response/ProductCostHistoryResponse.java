package com.br.food.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.br.food.models.ProductCostHistory;

public class ProductCostHistoryResponse {

	private final Long id;
	private final BigDecimal costPrice;
	private final LocalDateTime recordedAt;

	public ProductCostHistoryResponse(ProductCostHistory history) {
		this.id = history.getId();
		this.costPrice = history.getCostPrice();
		this.recordedAt = history.getRecordedAt();
	}

	public Long getId() {
		return id;
	}

	public BigDecimal getCostPrice() {
		return costPrice;
	}

	public LocalDateTime getRecordedAt() {
		return recordedAt;
	}
}
