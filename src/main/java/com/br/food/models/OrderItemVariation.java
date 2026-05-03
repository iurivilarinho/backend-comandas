package com.br.food.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

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
@Table(name = "order_item_variation")
public class OrderItemVariation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_order_item_id", foreignKey = @ForeignKey(name = "fk_order_item_variation_order_item"))
	private OrderItem orderItem;

	@Column(name = "product_variation_id", nullable = false)
	private Long productVariationId;

	@Column(name = "variation_name", nullable = false, length = 100)
	private String variationName;

	@Column(name = "variation_price_delta", nullable = false, precision = 12, scale = 2)
	private BigDecimal priceDelta;

	@Column(name = "group_title", nullable = false, length = 60)
	private String groupTitle;

	@Column(name = "display_order", nullable = false)
	private Integer displayOrder;

	@Column(name = "created_at", updatable = false, nullable = false)
	private LocalDateTime createdAt;

	public OrderItemVariation() {
	}

	public OrderItemVariation(OrderItem orderItem, ProductVariation variation, Integer displayOrder) {
		this.orderItem = orderItem;
		this.productVariationId = variation.getId();
		this.variationName = variation.getName();
		this.priceDelta = variation.getPriceDelta();
		this.groupTitle = variation.getGroup() != null ? variation.getGroup().getTitle() : "";
		this.displayOrder = displayOrder;
	}

	@PrePersist
	private void prePersist() {
		this.createdAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public OrderItem getOrderItem() {
		return orderItem;
	}

	public void setOrderItem(OrderItem orderItem) {
		this.orderItem = orderItem;
	}

	public Long getProductVariationId() {
		return productVariationId;
	}

	public String getVariationName() {
		return variationName;
	}

	public BigDecimal getPriceDelta() {
		return priceDelta;
	}

	public String getGroupTitle() {
		return groupTitle;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof OrderItemVariation other)) {
			return false;
		}
		return id != null && Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
