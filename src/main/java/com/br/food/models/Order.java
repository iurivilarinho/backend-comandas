package com.br.food.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.br.food.enums.Types.OrderChannel;
import com.br.food.enums.Types.OrderStatus;
import com.br.food.request.OrderRequest;

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
@Table(name = "orders")
@Schema(description = "Customer order registered by the restaurant")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_customer_id", foreignKey = @ForeignKey(name = "fk_order_customer"))
	private Customer customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_table_id", foreignKey = @ForeignKey(name = "fk_order_table"))
	private DiningTable diningTable;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> items = new ArrayList<>();

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderPayment> payments = new ArrayList<>();

	@Column(name = "code", nullable = false, length = 10, unique = true)
	private String code;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private OrderStatus status;

	@Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
	private BigDecimal discountPercentage;

	@Column(name = "subtotal_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal subtotalAmount;

	@Column(name = "service_fee_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal serviceFeeAmount;

	@Column(name = "cover_charge_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal coverChargeAmount;

	@Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal paidAmount;

	@Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal totalAmount;

	@Column(name = "split_by_person_count")
	private Integer splitByPersonCount;

	@Column(name = "opened_at", nullable = false)
	private LocalDateTime openedAt;

	@Column(name = "closed_at")
	private LocalDateTime closedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "channel", nullable = false)
	private OrderChannel channel;

	@Column(name = "created_at", updatable = false, nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public Order() {
	}

	public Order(OrderRequest request, Customer customer, String code, DiningTable diningTable) {
		this.customer = customer;
		this.code = code;
		this.diningTable = diningTable;
		this.status = OrderStatus.OPEN;
		this.channel = request.getChannel();
		this.discountPercentage = request.getDiscountPercentage() != null ? request.getDiscountPercentage() : BigDecimal.ZERO;
		this.subtotalAmount = BigDecimal.ZERO;
		this.serviceFeeAmount = BigDecimal.ZERO;
		this.coverChargeAmount = BigDecimal.ZERO;
		this.paidAmount = BigDecimal.ZERO;
		this.totalAmount = BigDecimal.ZERO;
		this.openedAt = LocalDateTime.now();
	}

	public void update(OrderRequest request, DiningTable diningTable) {
		this.diningTable = diningTable;
		this.channel = request.getChannel();
		this.discountPercentage = request.getDiscountPercentage() != null ? request.getDiscountPercentage() : BigDecimal.ZERO;
	}

	@PrePersist
	private void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
		if (this.openedAt == null) {
			this.openedAt = now;
		}
		if (this.discountPercentage == null) {
			this.discountPercentage = BigDecimal.ZERO;
		}
		if (this.subtotalAmount == null) {
			this.subtotalAmount = BigDecimal.ZERO;
		}
		if (this.serviceFeeAmount == null) {
			this.serviceFeeAmount = BigDecimal.ZERO;
		}
		if (this.coverChargeAmount == null) {
			this.coverChargeAmount = BigDecimal.ZERO;
		}
		if (this.paidAmount == null) {
			this.paidAmount = BigDecimal.ZERO;
		}
		if (this.totalAmount == null) {
			this.totalAmount = BigDecimal.ZERO;
		}
	}

	@PreUpdate
	private void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public DiningTable getDiningTable() {
		return diningTable;
	}

	public void setDiningTable(DiningTable diningTable) {
		this.diningTable = diningTable;
	}

	public List<OrderItem> getItems() {
		return items;
	}

	public void setItems(List<OrderItem> items) {
		this.items = items;
	}

	public List<OrderPayment> getPayments() {
		return payments;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public BigDecimal getDiscountPercentage() {
		return discountPercentage;
	}

	public void setDiscountPercentage(BigDecimal discountPercentage) {
		this.discountPercentage = discountPercentage;
	}

	public BigDecimal getSubtotalAmount() {
		return subtotalAmount;
	}

	public void setSubtotalAmount(BigDecimal subtotalAmount) {
		this.subtotalAmount = subtotalAmount;
	}

	public BigDecimal getServiceFeeAmount() {
		return serviceFeeAmount;
	}

	public void setServiceFeeAmount(BigDecimal serviceFeeAmount) {
		this.serviceFeeAmount = serviceFeeAmount;
	}

	public BigDecimal getCoverChargeAmount() {
		return coverChargeAmount;
	}

	public void setCoverChargeAmount(BigDecimal coverChargeAmount) {
		this.coverChargeAmount = coverChargeAmount;
	}

	public BigDecimal getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(BigDecimal paidAmount) {
		this.paidAmount = paidAmount;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public Integer getSplitByPersonCount() {
		return splitByPersonCount;
	}

	public void setSplitByPersonCount(Integer splitByPersonCount) {
		this.splitByPersonCount = splitByPersonCount;
	}

	public LocalDateTime getOpenedAt() {
		return openedAt;
	}

	public void setOpenedAt(LocalDateTime openedAt) {
		this.openedAt = openedAt;
	}

	public LocalDateTime getClosedAt() {
		return closedAt;
	}

	public void setClosedAt(LocalDateTime closedAt) {
		this.closedAt = closedAt;
	}

	public OrderChannel getChannel() {
		return channel;
	}

	public void setChannel(OrderChannel channel) {
		this.channel = channel;
	}
}
