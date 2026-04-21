package com.br.food.response;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import com.br.food.util.excel.ExcelColumn;

public class FinancialReportRowResponse {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	@ExcelColumn("Data")
	private final String occurredAt;

	@ExcelColumn("Origem")
	private final String origin;

	@ExcelColumn("Tipo")
	private final String type;

	@ExcelColumn("Categoria")
	private final String category;

	@ExcelColumn("Descricao")
	private final String description;

	@ExcelColumn("Referencia")
	private final String referenceCode;

	@ExcelColumn("Pagamento")
	private final String paymentMethod;

	@ExcelColumn("Valor")
	private final BigDecimal amount;

	@ExcelColumn("Observacoes")
	private final String notes;

	@ExcelColumn("Detalhamento")
	private final String breakdown;

	public FinancialReportRowResponse(FinancialEntryResponse entry) {
		this.occurredAt = entry.getOccurredAt() != null ? entry.getOccurredAt().format(DATE_TIME_FORMATTER) : "";
		this.origin = entry.getOriginLabel();
		this.type = entry.getTypeLabel();
		this.category = entry.getCategoryLabel();
		this.description = entry.getDescription();
		this.referenceCode = entry.getReferenceCode();
		this.paymentMethod = entry.getPaymentMethodLabel();
		this.amount = entry.getAmount();
		this.notes = entry.getNotes();
		this.breakdown = entry.getBreakdown().stream()
				.map(item -> item.getLabel() + ": " + item.getAmount())
				.reduce((left, right) -> left + " | " + right)
				.orElse("");
	}
}
