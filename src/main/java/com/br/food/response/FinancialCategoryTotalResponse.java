package com.br.food.response;

import java.math.BigDecimal;

import com.br.food.enums.Types.FinanceCategory;
import com.br.food.enums.Types.FinanceEntryType;

public class FinancialCategoryTotalResponse {

	private final FinanceEntryType type;
	private final FinanceCategory category;
	private final BigDecimal amount;

	public FinancialCategoryTotalResponse(FinanceEntryType type, FinanceCategory category, BigDecimal amount) {
		this.type = type;
		this.category = category;
		this.amount = amount;
	}

	public FinanceEntryType getType() {
		return type;
	}

	public FinanceCategory getCategory() {
		return category;
	}

	public BigDecimal getAmount() {
		return amount;
	}
}
