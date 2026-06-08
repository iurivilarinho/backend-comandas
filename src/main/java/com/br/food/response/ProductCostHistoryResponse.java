package com.br.food.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.br.food.enums.Types.CostPriceSource;
import com.br.food.models.ProductCostHistory;

public class ProductCostHistoryResponse {

	private final Long id;
	private final BigDecimal costPrice;
	private final CostPriceSource source;
	private final String invoiceNumber;
	private final LocalDateTime recordedAt;

	public ProductCostHistoryResponse(ProductCostHistory history) {
		this.id = history.getId();
		this.costPrice = history.getCostPrice();
		this.source = history.getSource() != null ? history.getSource() : CostPriceSource.MANUAL;
		this.invoiceNumber = history.getInvoiceNumber();
		this.recordedAt = history.getRecordedAt();
	}

	public Long getId() {
		return id;
	}

	public BigDecimal getCostPrice() {
		return costPrice;
	}

	public CostPriceSource getSource() {
		return source;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public LocalDateTime getRecordedAt() {
		return recordedAt;
	}
}
