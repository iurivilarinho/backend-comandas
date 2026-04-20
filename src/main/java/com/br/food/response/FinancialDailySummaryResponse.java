package com.br.food.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FinancialDailySummaryResponse {

	private final LocalDate date;
	private final BigDecimal income;
	private final BigDecimal expense;
	private final BigDecimal balance;

	public FinancialDailySummaryResponse(LocalDate date, BigDecimal income, BigDecimal expense, BigDecimal balance) {
		this.date = date;
		this.income = income;
		this.expense = expense;
		this.balance = balance;
	}

	public LocalDate getDate() {
		return date;
	}

	public BigDecimal getIncome() {
		return income;
	}

	public BigDecimal getExpense() {
		return expense;
	}

	public BigDecimal getBalance() {
		return balance;
	}
}
