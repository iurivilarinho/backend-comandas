package com.br.food.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.br.food.enums.Types.FinanceCategory;
import com.br.food.enums.Types.FinanceEntryType;
import com.br.food.enums.Types.FinanceOrigin;
import com.br.food.enums.Types.PaymentMethod;
import com.br.food.models.FinancialEntry;
import com.br.food.util.FinancialLabelUtils;

public class FinancialEntryResponse {

	private final String id;
	private final FinanceOrigin origin;
	private final String originLabel;
	private final FinanceEntryType type;
	private final String typeLabel;
	private final FinanceCategory category;
	private final String categoryLabel;
	private final String description;
	private final String referenceCode;
	private final String notes;
	private final BigDecimal amount;
	private final PaymentMethod paymentMethod;
	private final String paymentMethodLabel;
	private final LocalDateTime occurredAt;
	private final List<FinancialBreakdownResponse> breakdown;

	public FinancialEntryResponse(
			String id,
			FinanceOrigin origin,
			FinanceEntryType type,
			FinanceCategory category,
			String description,
			String referenceCode,
			String notes,
			BigDecimal amount,
			PaymentMethod paymentMethod,
			LocalDateTime occurredAt,
			List<FinancialBreakdownResponse> breakdown) {
		this.id = id;
		this.origin = origin;
		this.originLabel = FinancialLabelUtils.origin(origin);
		this.type = type;
		this.typeLabel = FinancialLabelUtils.type(type);
		this.category = category;
		this.categoryLabel = FinancialLabelUtils.category(category);
		this.description = description;
		this.referenceCode = referenceCode;
		this.notes = notes;
		this.amount = amount;
		this.paymentMethod = paymentMethod;
		this.paymentMethodLabel = FinancialLabelUtils.paymentMethod(paymentMethod);
		this.occurredAt = occurredAt;
		this.breakdown = breakdown;
	}

	public FinancialEntryResponse(FinancialEntry entry) {
		this(
				"manual-" + entry.getId(),
				FinanceOrigin.MANUAL,
				entry.getType(),
				entry.getCategory(),
				entry.getDescription(),
				entry.getId() != null ? "#" + entry.getId() : null,
				entry.getNotes(),
				entry.getAmount(),
				entry.getPaymentMethod(),
				entry.getOccurredAt(),
				List.of(new FinancialBreakdownResponse(FinancialLabelUtils.category(entry.getCategory()), entry.getAmount())));
	}

	public String getId() {
		return id;
	}

	public FinanceOrigin getOrigin() {
		return origin;
	}

	public String getOriginLabel() {
		return originLabel;
	}

	public FinanceEntryType getType() {
		return type;
	}

	public String getTypeLabel() {
		return typeLabel;
	}

	public FinanceCategory getCategory() {
		return category;
	}

	public String getCategoryLabel() {
		return categoryLabel;
	}

	public String getDescription() {
		return description;
	}

	public String getReferenceCode() {
		return referenceCode;
	}

	public String getNotes() {
		return notes;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public String getPaymentMethodLabel() {
		return paymentMethodLabel;
	}

	public LocalDateTime getOccurredAt() {
		return occurredAt;
	}

	public List<FinancialBreakdownResponse> getBreakdown() {
		return breakdown;
	}
}
