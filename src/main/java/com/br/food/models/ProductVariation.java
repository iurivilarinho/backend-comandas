package com.br.food.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import com.br.food.request.ProductVariationRequest;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_variation")
public class ProductVariation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_product_id", foreignKey = @ForeignKey(name = "fk_product_variation_product"))
	private Product product;

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@Column(name = "price_delta", nullable = false, precision = 12, scale = 2)
	private BigDecimal priceDelta;

	@Column(name = "active", nullable = false)
	private Boolean active;

	@Column(name = "display_order", nullable = false)
	private Integer displayOrder;

	@Column(name = "created_at", updatable = false, nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public ProductVariation() {
	}

	public ProductVariation(Product product, ProductVariationRequest request, Integer displayOrder) {
		this.product = product;
		this.name = request.getName().trim();
		this.priceDelta = request.getPriceDelta();
		this.active = request.getResolvedActive();
		this.displayOrder = displayOrder;
	}

	public void update(ProductVariationRequest request, Integer displayOrder) {
		this.name = request.getName().trim();
		this.priceDelta = request.getPriceDelta();
		this.active = request.getResolvedActive();
		this.displayOrder = displayOrder;
	}

	@PrePersist
	private void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	private void preUpdate() {
		this.updatedAt = LocalDateTime.now();
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

	public String getName() {
		return name;
	}

	public BigDecimal getPriceDelta() {
		return priceDelta;
	}

	public Boolean getActive() {
		return active;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof ProductVariation other)) {
			return false;
		}
		return id != null && Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
