package com.br.food.response;

import com.br.food.util.excel.ExcelColumn;

public class FinancialReportExcelRow {

	@ExcelColumn("Data/Hora")
	private final String occurredAt;

	@ExcelColumn("Origem")
	private final String origin;

	@ExcelColumn("Tipo")
	private final String type;

	@ExcelColumn("Categoria")
	private final String category;

	@ExcelColumn("Descrição")
	private final String description;

	@ExcelColumn("Referência")
	private final String referenceCode;

	@ExcelColumn("Forma de pagamento")
	private final String paymentMethod;

	@ExcelColumn("Valor")
	private final String amount;

	@ExcelColumn("Detalhamento")
	private final String breakdown;

	@ExcelColumn("Observações")
	private final String notes;

	public FinancialReportExcelRow(
			String occurredAt,
			String origin,
			String type,
			String category,
			String description,
			String referenceCode,
			String paymentMethod,
			String amount,
			String breakdown,
			String notes) {
		this.occurredAt = occurredAt;
		this.origin = origin;
		this.type = type;
		this.category = category;
		this.description = description;
		this.referenceCode = referenceCode;
		this.paymentMethod = paymentMethod;
		this.amount = amount;
		this.breakdown = breakdown;
		this.notes = notes;
	}
}
