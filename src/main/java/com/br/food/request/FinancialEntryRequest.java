package com.br.food.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.br.food.enums.Types.FinanceCategory;
import com.br.food.enums.Types.FinanceEntryType;
import com.br.food.enums.Types.PaymentMethod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FinancialEntryRequest {

	@NotNull(message = "Entry type is required.")
	private FinanceEntryType type;

	@NotNull(message = "Category is required.")
	private FinanceCategory category;

	private PaymentMethod paymentMethod;

	@NotBlank(message = "Description is required.")
	@Size(max = 255, message = "Description must have at most 255 characters.")
	private String description;

	@Size(max = 500, message = "Notes must have at most 500 characters.")
	private String notes;

	@NotNull(message = "Amount is required.")
	@DecimalMin(value = "0.01", message = "Amount must be greater than zero.")
	private BigDecimal amount;

	private LocalDateTime occurredAt;

	public FinanceEntryType getType() {
		return type;
	}

	public FinanceCategory getCategory() {
		return category;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public String getDescription() {
		return description;
	}

	public String getNotes() {
		return notes;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public LocalDateTime getOccurredAt() {
		return occurredAt;
	}
}
