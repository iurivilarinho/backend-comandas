package com.br.food.models;

import java.math.BigDecimal;

import com.br.food.enums.Types.PaymentMethod;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
@Entity
@Table(name = "tbPayment")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private PaymentMethod tipoPagamento;

	@Column(nullable = false)
	private BigDecimal valorPago;

	public Payment() {
	}

	public Payment(PaymentMethod tipoPagamento, BigDecimal valorPago) {
		this.tipoPagamento = tipoPagamento;
		this.valorPago = valorPago;
	}

	public Long getId() {
		return id;
	}

	public PaymentMethod getTipoPagamento() {
		return tipoPagamento;
	}

	public PaymentMethod getPaymentMethod() {
		return tipoPagamento;
	}

	public void setTipoPagamento(PaymentMethod tipoPagamento) {
		this.tipoPagamento = tipoPagamento;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.tipoPagamento = paymentMethod;
	}

	public BigDecimal getValorPago() {
		return valorPago;
	}

	public BigDecimal getPaidAmount() {
		return valorPago;
	}

	public void setValorPago(BigDecimal valorPago) {
		this.valorPago = valorPago;
	}

	public void setPaidAmount(BigDecimal paidAmount) {
		this.valorPago = paidAmount;
	}

}
