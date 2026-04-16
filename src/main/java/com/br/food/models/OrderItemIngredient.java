package com.br.food.models;

import java.math.BigDecimal;

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
@Table(name = "order_item_ingredients")
@Schema(description = "Customized ingredient selection linked to one order item")
public class OrderItemIngredient {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_order_item_id", foreignKey = @ForeignKey(name = "fk_order_item_ingredient_order_item"))
	private OrderItem orderItem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_ingredient_product_id", foreignKey = @ForeignKey(name = "fk_order_item_ingredient_product"))
	private Product ingredientProduct;

	@Column(name = "quantity", nullable = false)
	private BigDecimal quantity;

	@Column(name = "base_quantity", nullable = false)
	private BigDecimal baseQuantity;

	public OrderItemIngredient() {
	}

	public OrderItemIngredient(OrderItem orderItem, Product ingredientProduct, BigDecimal quantity, BigDecimal baseQuantity) {
		this.orderItem = orderItem;
		this.ingredientProduct = ingredientProduct;
		this.quantity = quantity;
		this.baseQuantity = baseQuantity;
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

	public Product getIngredientProduct() {
		return ingredientProduct;
	}

	public void setIngredientProduct(Product ingredientProduct) {
		this.ingredientProduct = ingredientProduct;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getBaseQuantity() {
		return baseQuantity;
	}

	public void setBaseQuantity(BigDecimal baseQuantity) {
		this.baseQuantity = baseQuantity;
	}
}
