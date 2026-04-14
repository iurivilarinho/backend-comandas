package com.br.food.models;

import java.math.BigDecimal;

import com.br.food.request.StockEntryRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock_entries")
@Schema(description = "Inbound stock lot for a product")
public class StockEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_product_id", foreignKey = @ForeignKey(name = "fk_stock_entry_product"))
	private Product product;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_supply_invoice_id", foreignKey = @ForeignKey(name = "fk_stock_entry_supply_invoice"))
	private SupplyInvoice supplyInvoice;

	@Column(name = "batch", length = 50, nullable = false)
	private String batch;

	@Column(name = "available_quantity", nullable = false)
	private BigDecimal availableQuantity;

	@Column(name = "reserved_quantity", nullable = false)
	private BigDecimal reservedQuantity;

	@Column(name = "sold_quantity", nullable = false)
	private BigDecimal soldQuantity;

	@Column(name = "input_quantity", nullable = false)
	private BigDecimal inputQuantity;

	public StockEntry() {
	}

	public StockEntry(StockEntryRequest request, Product product, SupplyInvoice supplyInvoice) {
		this.product = product;
		this.supplyInvoice = supplyInvoice;
		this.batch = request.getBatch();
		this.availableQuantity = request.getQuantity();
		this.reservedQuantity = BigDecimal.ZERO;
		this.soldQuantity = BigDecimal.ZERO;
		this.inputQuantity = request.getQuantity();
	}

	public Long getId() {
		return id;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public SupplyInvoice getSupplyInvoice() {
		return supplyInvoice;
	}

	public void setSupplyInvoice(SupplyInvoice supplyInvoice) {
		this.supplyInvoice = supplyInvoice;
	}

	public String getBatch() {
		return batch;
	}

	public void setBatch(String batch) {
		this.batch = batch;
	}

	public BigDecimal getAvailableQuantity() {
		return availableQuantity;
	}

	public void setAvailableQuantity(BigDecimal availableQuantity) {
		this.availableQuantity = availableQuantity;
	}

	public BigDecimal getReservedQuantity() {
		return reservedQuantity;
	}

	public void setReservedQuantity(BigDecimal reservedQuantity) {
		this.reservedQuantity = reservedQuantity;
	}

	public BigDecimal getSoldQuantity() {
		return soldQuantity;
	}

	public void setSoldQuantity(BigDecimal soldQuantity) {
		this.soldQuantity = soldQuantity;
	}

	public BigDecimal getInputQuantity() {
		return inputQuantity;
	}

	public void setInputQuantity(BigDecimal inputQuantity) {
		this.inputQuantity = inputQuantity;
	}
}
