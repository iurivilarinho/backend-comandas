package com.br.food.response;

import java.math.BigDecimal;

import com.br.food.enums.Types.FinanceCategory;
import com.br.food.enums.Types.FinanceEntryType;
import com.br.food.util.FinancialLabelUtils;

public class FinancialCategoryTotalResponse {

	private final FinanceEntryType type;
	private final String typeLabel;
	private final FinanceCategory category;
	private final String categoryLabel;
	private final BigDecimal amount;

	public FinancialCategoryTotalResponse(FinanceEntryType type, FinanceCategory category, BigDecimal amount) {
		this.type = type;
		this.typeLabel = FinancialLabelUtils.type(type);
		this.category = category;
		this.categoryLabel = FinancialLabelUtils.category(category);
		this.amount = amount;
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

	public BigDecimal getAmount() {
		return amount;
	}
}
