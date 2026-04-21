package com.br.food.response;

import java.math.BigDecimal;
import java.util.List;

public class FinancialOverviewResponse {

	private final BigDecimal totalIncome;
	private final BigDecimal totalExpense;
	private final BigDecimal balance;
	private final List<FinancialEntryResponse> entries;
	private final PageMetadataResponse entriesPagination;
	private final List<FinancialDailySummaryResponse> dailySummary;
	private final List<FinancialCategoryTotalResponse> categoryTotals;
	private final List<FinancialPaymentMethodTotalResponse> paymentMethodTotals;

	public FinancialOverviewResponse(
			BigDecimal totalIncome,
			BigDecimal totalExpense,
			BigDecimal balance,
			List<FinancialEntryResponse> entries,
			PageMetadataResponse entriesPagination,
			List<FinancialDailySummaryResponse> dailySummary,
			List<FinancialCategoryTotalResponse> categoryTotals,
			List<FinancialPaymentMethodTotalResponse> paymentMethodTotals) {
		this.totalIncome = totalIncome;
		this.totalExpense = totalExpense;
		this.balance = balance;
		this.entries = entries;
		this.entriesPagination = entriesPagination;
		this.dailySummary = dailySummary;
		this.categoryTotals = categoryTotals;
		this.paymentMethodTotals = paymentMethodTotals;
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

	public PageMetadataResponse getEntriesPagination() {
		return entriesPagination;
	}

	public List<FinancialDailySummaryResponse> getDailySummary() {
		return dailySummary;
	}

	public List<FinancialCategoryTotalResponse> getCategoryTotals() {
		return categoryTotals;
	}

	public List<FinancialPaymentMethodTotalResponse> getPaymentMethodTotals() {
		return paymentMethodTotals;
	}
}
