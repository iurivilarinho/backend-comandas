package com.br.food.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.br.food.enums.Types.SupplyInvoiceStatus;
import com.br.food.request.SupplyInvoiceRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "supply_invoices", uniqueConstraints = @UniqueConstraint(columnNames = { "invoice_number", "series_number",
		"access_key" }))
@Schema(description = "Supplier invoice used to register stock entries")
public class SupplyInvoice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "invoice_number", length = 20, nullable = false)
	private String invoiceNumber;

	@Column(name = "series_number", length = 20, nullable = false)
	private String seriesNumber;

	@Column(name = "access_key", length = 44, nullable = false)
	private String accessKey;

	@Column(name = "issue_date", nullable = false)
	private LocalDate issueDate;

	@OneToMany(mappedBy = "supplyInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<StockEntry> items = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private SupplyInvoiceStatus status;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "fk_attachment_id", foreignKey = @ForeignKey(name = "fk_supply_invoice_document"))
	private Document attachment;

	public SupplyInvoice() {
	}

	public SupplyInvoice(SupplyInvoiceRequest request, Document attachment) {
		this.invoiceNumber = request.getInvoiceNumber();
		this.seriesNumber = request.getSeriesNumber();
		this.accessKey = request.getAccessKey();
		this.issueDate = request.getIssueDate();
		this.attachment = attachment;
		this.status = SupplyInvoiceStatus.ALLOCATED;
	}

	public void update(SupplyInvoiceRequest request, Document attachment) {
		this.invoiceNumber = request.getInvoiceNumber();
		this.seriesNumber = request.getSeriesNumber();
		this.accessKey = request.getAccessKey();
		this.issueDate = request.getIssueDate();
		if (attachment != null) {
			this.attachment = attachment;
		}
	}

	public Long getId() {
		return id;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public String getSeriesNumber() {
		return seriesNumber;
	}

	public void setSeriesNumber(String seriesNumber) {
		this.seriesNumber = seriesNumber;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public LocalDate getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(LocalDate issueDate) {
		this.issueDate = issueDate;
	}

	public List<StockEntry> getItems() {
		return items;
	}

	public void setItems(List<StockEntry> items) {
		this.items = items;
	}

	public SupplyInvoiceStatus getStatus() {
		return status;
	}

	public void setStatus(SupplyInvoiceStatus status) {
		this.status = status;
	}

	public Document getAttachment() {
		return attachment;
	}

	public void setAttachment(Document attachment) {
		this.attachment = attachment;
	}
}
