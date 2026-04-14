package com.br.food.response;

import java.math.BigDecimal;

import com.br.food.models.StockEntry;

public class StockEntryResponse {

	private final Long id;
	private final ProductBasicResponse product;
	private final String batch;
	private final BigDecimal availableQuantity;
	private final BigDecimal reservedQuantity;
	private final BigDecimal soldQuantity;
	private final BigDecimal inputQuantity;

	public StockEntryResponse(StockEntry stockEntry) {
		this.id = stockEntry.getId();
		this.product = new ProductBasicResponse(stockEntry.getProduct());
		this.batch = stockEntry.getBatch();
		this.availableQuantity = stockEntry.getAvailableQuantity();
		this.reservedQuantity = stockEntry.getReservedQuantity();
		this.soldQuantity = stockEntry.getSoldQuantity();
		this.inputQuantity = stockEntry.getInputQuantity();
	}

	public Long getId() {
		return id;
	}

	public ProductBasicResponse getProduct() {
		return product;
	}

	public String getBatch() {
		return batch;
	}

	public BigDecimal getAvailableQuantity() {
		return availableQuantity;
	}

	public BigDecimal getReservedQuantity() {
		return reservedQuantity;
	}

	public BigDecimal getSoldQuantity() {
		return soldQuantity;
	}

	public BigDecimal getInputQuantity() {
		return inputQuantity;
	}
}
