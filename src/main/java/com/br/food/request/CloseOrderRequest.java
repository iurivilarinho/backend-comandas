package com.br.food.request;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class CloseOrderRequest {

	@Valid
	@NotEmpty(message = "At least one payment line is required.")
	private List<PaymentLineRequest> payments = new ArrayList<>();

	@Min(value = 1, message = "Split by person count must be at least one.")
	private Integer splitByPersonCount;

	@DecimalMin(value = "0.0", message = "Discount percentage cannot be negative.")
	private BigDecimal discountPercentage;

	@DecimalMin(value = "0.0", message = "Discount amount cannot be negative.")
	private BigDecimal discountAmount;

	@Size(max = 255, message = "Notes must have at most 255 characters.")
	private String notes;

	public List<PaymentLineRequest> getPayments() {
		return payments;
	}

	public Integer getSplitByPersonCount() {
		return splitByPersonCount;
	}

	public BigDecimal getDiscountPercentage() {
		return discountPercentage;
	}

	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}

	public String getNotes() {
		return notes;
	}
}
