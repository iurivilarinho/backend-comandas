package com.br.food.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.br.food.enums.Types.FinanceCategory;
import com.br.food.enums.Types.FinanceEntryType;
import com.br.food.util.FinancialLabelUtils;
import com.br.food.util.excel.ExcelColumn;

public class FinancialReportSummaryResponse {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	@ExcelColumn("Data inicial")
	private final String startDate;

	@ExcelColumn("Data final")
	private final String endDate;

	@ExcelColumn("Tipo")
	private final String type;

	@ExcelColumn("Categoria")
	private final String category;

	@ExcelColumn("Total de entradas")
	private final BigDecimal totalIncome;

	@ExcelColumn("Total de saídas")
	private final BigDecimal totalExpense;

	@ExcelColumn("Saldo")
	private final BigDecimal balance;

	@ExcelColumn("Total de lançamentos")
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
		this.startDate = startDate != null ? startDate.format(DATE_FORMATTER) : "Todos";
		this.endDate = endDate != null ? endDate.format(DATE_FORMATTER) : "Todos";
		this.type = type != null ? FinancialLabelUtils.type(type) : "Todos";
		this.category = category != null ? FinancialLabelUtils.category(category) : "Todas";
		this.totalIncome = totalIncome;
		this.totalExpense = totalExpense;
		this.balance = balance;
		this.totalEntries = totalEntries;
	}

	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public String getType() {
		return type;
	}

	public String getCategory() {
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
