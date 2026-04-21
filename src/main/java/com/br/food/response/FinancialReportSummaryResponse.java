package com.br.food.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.br.food.enums.Types.FinanceCategory;
import com.br.food.enums.Types.FinanceEntryType;

public class FinancialReportSummaryResponse {

	private final LocalDate startDate;
	private final LocalDate endDate;
	private final FinanceEntryType type;
	private final FinanceCategory category;
	private final BigDecimal totalIncome;
	private final BigDecimal totalExpense;
	private final BigDecimal balance;
	private final Integer totalEntries;

	public FinancialReportSummaryResponse(
			LocalDate startDate,
			LocalDate endDate,
			FinanceEntryType type,
			FinanceCategory category,
			BigDecimal totalIncome,
			BigDecimal totalExpense,
			BigDecimal balance,
			Integer totalEntries) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.type = type;
		this.category = category;
		this.totalIncome = totalIncome;
		this.totalExpense = totalExpense;
		this.balance = balance;
		this.totalEntries = totalEntries;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public FinanceEntryType getType() {
		return type;
	}

	public FinanceCategory getCategory() {
		return category;
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

	public Integer getTotalEntries() {
		return totalEntries;
	}
}
