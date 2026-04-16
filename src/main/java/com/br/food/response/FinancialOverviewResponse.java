package com.br.food.response;

import java.math.BigDecimal;
import java.util.List;

public class FinancialOverviewResponse {

	private final BigDecimal totalIncome;
	private final BigDecimal totalExpense;
	private final BigDecimal balance;
	private final List<FinancialEntryResponse> entries;

	public FinancialOverviewResponse(
			BigDecimal totalIncome,
			BigDecimal totalExpense,
			BigDecimal balance,
			List<FinancialEntryResponse> entries) {
		this.totalIncome = totalIncome;
		this.totalExpense = totalExpense;
		this.balance = balance;
		this.entries = entries;
	}

	public BigDecimal getTotalIncome() {
		return totalIncome;
	}

	public BigDecimal getTotalExpense() {
		return totalExpense;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public List<FinancialEntryResponse> getEntries() {
		return entries;
	}
}
