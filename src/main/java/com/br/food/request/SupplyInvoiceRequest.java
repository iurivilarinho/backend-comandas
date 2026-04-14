package com.br.food.request;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Supply invoice payload")
public class SupplyInvoiceRequest {

	@NotNull(message = "Invoice number is required.")
	@Size(min = 1, max = 20, message = "Invoice number must have between 1 and 20 characters.")
	private String invoiceNumber;

	@NotNull(message = "Series number is required.")
	@Size(min = 1, max = 10, message = "Series number must have between 1 and 10 characters.")
	private String seriesNumber;

	@NotNull(message = "Access key is required.")
	@Size(min = 44, max = 44, message = "Access key must have exactly 44 characters.")
	@Pattern(regexp = "^[0-9]*$", message = "Access key must contain only numbers.")
	private String accessKey;

	@NotNull(message = "Issue date is required.")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate issueDate;

	@NotEmpty(message = "At least one stock entry is required.")
	@Valid
	private List<StockEntryRequest> items = new ArrayList<>();

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public String getSeriesNumber() {
		return seriesNumber;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public LocalDate getIssueDate() {
		return issueDate;
	}

	public List<StockEntryRequest> getItems() {
		return items;
	}
}
