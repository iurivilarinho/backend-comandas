package com.br.food.response;

import java.time.LocalDate;
import java.util.List;

import com.br.food.enums.Types.SupplyInvoiceStatus;
import com.br.food.models.SupplyInvoice;

public class SupplyInvoiceResponse {

	private final Long id;
	private final String invoiceNumber;
	private final String seriesNumber;
	private final String accessKey;
	private final LocalDate issueDate;
	private final LocalDate launchDate;
	private final SupplyInvoiceStatus status;
	private final DocumentResponse attachment;
	private final List<StockEntryResponse> items;

	public SupplyInvoiceResponse(SupplyInvoice invoice) {
		this.id = invoice.getId();
		this.invoiceNumber = invoice.getInvoiceNumber();
		this.seriesNumber = invoice.getSeriesNumber();
		this.accessKey = invoice.getAccessKey();
		this.issueDate = invoice.getIssueDate();
		this.launchDate = invoice.getLaunchDate();
		this.status = invoice.getStatus();
		this.attachment = invoice.getAttachment() != null ? new DocumentResponse(invoice.getAttachment()) : null;
		this.items = invoice.getItems().stream().map(StockEntryResponse::new).toList();
	}

	public Long getId() {
		return id;
	}

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

	public LocalDate getLaunchDate() {
		return launchDate;
	}

	public SupplyInvoiceStatus getStatus() {
		return status;
	}

	public DocumentResponse getAttachment() {
		return attachment;
	}

	public List<StockEntryResponse> getItems() {
		return items;
	}
}
