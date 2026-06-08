package com.br.food.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_cost_history")
@Schema(description = "Historical record of a product cost price change")
public class ProductCostHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_product_id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_product_cost_history_product"))
	private Product product;

	@Column(name = "cost_price", nullable = false)
	private BigDecimal costPrice;

	@Column(name = "recorded_at", updatable = false, nullable = false)
	private LocalDateTime recordedAt;

	public ProductCostHistory() {
	}

	public ProductCostHistory(Product product, BigDecimal costPrice) {
		this.product = product;
		this.costPrice = costPrice;
	}

	@PrePersist
	private void prePersist() {
		this.recordedAt = LocalDateTime.now();
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

	public BigDecimal getCostPrice() {
		return costPrice;
	}

	public void setCostPrice(BigDecimal costPrice) {
		this.costPrice = costPrice;
	}

	public LocalDateTime getRecordedAt() {
		return recordedAt;
	}
}
