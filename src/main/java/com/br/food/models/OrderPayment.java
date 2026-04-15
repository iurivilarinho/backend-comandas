package com.br.food.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.br.food.enums.Types.PaymentMethod;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_payments")
public class OrderPayment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_order_id", foreignKey = @ForeignKey(name = "fk_order_payment_order"))
	private Order order;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", nullable = false)
	private PaymentMethod paymentMethod;

	@Column(name = "amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@Column(name = "cash_received", precision = 12, scale = 2)
	private BigDecimal cashReceived;

	@Column(name = "recorded_by", length = 100, nullable = false)
	private String recordedBy;

	@Column(name = "recorded_at", nullable = false)
	private LocalDateTime recordedAt;

	public OrderPayment() {
	}

	public OrderPayment(Order order, PaymentMethod paymentMethod, BigDecimal amount, BigDecimal cashReceived, String recordedBy) {
		this.order = order;
		this.paymentMethod = paymentMethod;
		this.amount = amount;
		this.cashReceived = cashReceived;
		this.recordedBy = recordedBy;
	}

	@PrePersist
	private void prePersist() {
		if (this.recordedAt == null) {
			this.recordedAt = LocalDateTime.now();
		}
	}

	public Long getId() {
		return id;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public BigDecimal getCashReceived() {
		return cashReceived;
	}

	public String getRecordedBy() {
		return recordedBy;
	}

	public LocalDateTime getRecordedAt() {
		return recordedAt;
	}
}
