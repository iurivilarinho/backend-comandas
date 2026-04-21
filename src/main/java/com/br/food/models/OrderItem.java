package com.br.food.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.request.OrderItemRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
@Schema(description = "Line item linked to an order")
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_product_id", foreignKey = @ForeignKey(name = "fk_order_item_product"))
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_order_id", foreignKey = @ForeignKey(name = "fk_order_item_order"))
	private Order order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_event_id", foreignKey = @ForeignKey(name = "fk_order_item_event"))
	private Event event;

	@OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<StockConsumption> stockConsumptions = new ArrayList<>();

	@OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItemIngredient> ingredients = new ArrayList<>();

	@Column(name = "quantity", nullable = false)
	private Integer quantity;

	@Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
	private BigDecimal unitPrice;

	@Column(name = "notes", length = 255)
	private String notes;

	@Column(name = "requested_at", nullable = false)
	private LocalDateTime requestedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private OrderItemStatus status;

	@Column(name = "decline_reason")
	private String declineReason;

	@Column(name = "cancellation_reason")
	private String cancellationReason;

	@Column(name = "created_at", updatable = false, nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public OrderItem() {
	}

	public OrderItem(Order order, Event event, Integer quantity) {
		this.order = order;
		this.event = event;
		this.quantity = quantity;
		this.unitPrice = event.getValue();
		this.status = OrderItemStatus.SERVED;
		this.requestedAt = LocalDateTime.now();
	}

	public OrderItem(Order order, Product product, OrderItemRequest request, BigDecimal unitPrice) {
		this.order = order;
		this.product = product;
		this.quantity = request.getQuantity();
		this.notes = request.getNotes();
		this.unitPrice = unitPrice;
		this.status = shouldGoToKitchen(product) ? OrderItemStatus.RECEIVED : OrderItemStatus.READY;
		this.requestedAt = LocalDateTime.now();
	}

	private boolean shouldGoToKitchen(Product product) {
		return product != null && Boolean.TRUE.equals(product.getSendToKitchen());
	}

	@PrePersist
	private void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
		if (this.requestedAt == null) {
			this.requestedAt = now;
		}
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

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public List<StockConsumption> getStockConsumptions() {
		return stockConsumptions;
	}

	public List<OrderItemIngredient> getIngredients() {
		return ingredients;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public LocalDateTime getRequestedAt() {
		return requestedAt;
	}

	public OrderItemStatus getStatus() {
		return status;
	}

	public void setStatus(OrderItemStatus status) {
		this.status = status;
	}

	public String getDeclineReason() {
		return declineReason;
	}

	public void setDeclineReason(String declineReason) {
		this.declineReason = declineReason;
	}

	public String getCancellationReason() {
		return cancellationReason;
	}

	public void setCancellationReason(String cancellationReason) {
		this.cancellationReason = cancellationReason;
	}
}
