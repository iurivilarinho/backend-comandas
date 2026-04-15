package com.br.food.models;

import java.math.BigDecimal;

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
@Table(name = "stock_consumptions")
public class StockConsumption {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_order_item_id", foreignKey = @ForeignKey(name = "fk_stock_consumption_order_item"))
	private OrderItem orderItem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_stock_entry_id", foreignKey = @ForeignKey(name = "fk_stock_consumption_stock_entry"))
	private StockEntry stockEntry;

	@Column(name = "quantity", nullable = false, precision = 12, scale = 3)
	private BigDecimal quantity;

	public StockConsumption() {
	}

	public StockConsumption(OrderItem orderItem, StockEntry stockEntry, BigDecimal quantity) {
		this.orderItem = orderItem;
		this.stockEntry = stockEntry;
		this.quantity = quantity;
	}

	public Long getId() {
		return id;
	}

	public OrderItem getOrderItem() {
		return orderItem;
	}

	public StockEntry getStockEntry() {
		return stockEntry;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}
}
