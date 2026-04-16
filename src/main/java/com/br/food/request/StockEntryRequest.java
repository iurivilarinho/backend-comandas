package com.br.food.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Stock entry payload")
public class StockEntryRequest {

	@NotNull(message = "Product id is required.")
	private Long productId;

	@NotNull(message = "Quantity is required.")
	@Positive(message = "Quantity must be greater than zero.")
	private BigDecimal quantity;

	@NotNull(message = "Batch is required.")
	@Size(min = 1, max = 50, message = "Batch must have between 1 and 50 characters.")
	private String batch;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate manufacturingDate;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate expirationDate;

	public Long getProductId() {
		return productId;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public String getBatch() {
		return batch;
	}

	public LocalDate getManufacturingDate() {
		return manufacturingDate;
	}

	public LocalDate getExpirationDate() {
		return expirationDate;
	}
}
