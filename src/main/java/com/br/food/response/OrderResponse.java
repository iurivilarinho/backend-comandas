package com.br.food.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.br.food.enums.Types.OrderChannel;
import com.br.food.enums.Types.OrderStatus;
import com.br.food.enums.Types.PaymentMethod;
import com.br.food.models.Order;

public class OrderResponse {

	private final Long id;
	private final String code;
	private final CustomerResponse customer;
	private final DiningTableResponse table;
	private final BigDecimal discountPercentage;
	private final BigDecimal discountAmount;
	private final BigDecimal subtotalAmount;
	private final BigDecimal serviceFeeAmount;
	private final BigDecimal coverChargeAmount;
	private final BigDecimal paidAmount;
	private final BigDecimal totalAmount;
	private final Integer splitByPersonCount;
	private final LocalDateTime openedAt;
	private final LocalDateTime closedAt;
	private final LocalDateTime checkoutRequestedAt;
	private final PaymentMethod requestedPaymentMethod;
	private final String checkoutRequestNotes;
	private final OrderStatus status;
	private final OrderChannel channel;
	private final List<OrderItemResponse> items;
	private final List<OrderPaymentResponse> payments;

	public OrderResponse(Order order) {
		this.id = order.getId();
		this.code = order.getCode();
		this.customer = order.getCustomer() != null ? new CustomerResponse(order.getCustomer()) : null;
		this.table = order.getDiningTable() != null ? new DiningTableResponse(order.getDiningTable()) : null;
		this.discountPercentage = order.getDiscountPercentage();
		this.discountAmount = order.getDiscountAmount();
		this.subtotalAmount = order.getSubtotalAmount();
		this.serviceFeeAmount = order.getServiceFeeAmount();
		this.coverChargeAmount = order.getCoverChargeAmount();
		this.paidAmount = order.getPaidAmount();
		this.totalAmount = order.getTotalAmount();
		this.splitByPersonCount = order.getSplitByPersonCount();
		this.openedAt = order.getOpenedAt();
		this.closedAt = order.getClosedAt();
		this.checkoutRequestedAt = order.getCheckoutRequestedAt();
		this.requestedPaymentMethod = order.getRequestedPaymentMethod();
		this.checkoutRequestNotes = order.getCheckoutRequestNotes();
		this.status = order.getStatus();
		this.channel = order.getChannel();
		this.items = order.getItems().stream().map(OrderItemResponse::new).toList();
		this.payments = order.getPayments().stream().map(OrderPaymentResponse::new).toList();
	}

	public Long getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public CustomerResponse getCustomer() {
		return customer;
	}

	public DiningTableResponse getTable() {
		return table;
	}

	public BigDecimal getDiscountPercentage() {
		return discountPercentage;
	}

	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}

	public BigDecimal getSubtotalAmount() {
		return subtotalAmount;
	}

	public BigDecimal getServiceFeeAmount() {
		return serviceFeeAmount;
	}

	public BigDecimal getCoverChargeAmount() {
		return coverChargeAmount;
	}

	public BigDecimal getPaidAmount() {
		return paidAmount;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public Integer getSplitByPersonCount() {
		return splitByPersonCount;
	}

	public LocalDateTime getOpenedAt() {
		return openedAt;
	}

	public LocalDateTime getClosedAt() {
		return closedAt;
	}

	public LocalDateTime getCheckoutRequestedAt() {
		return checkoutRequestedAt;
	}

	public PaymentMethod getRequestedPaymentMethod() {
		return requestedPaymentMethod;
	}

	public String getCheckoutRequestNotes() {
		return checkoutRequestNotes;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public OrderChannel getChannel() {
		return channel;
	}

	public List<OrderItemResponse> getItems() {
		return items;
	}

	public List<OrderPaymentResponse> getPayments() {
		return payments;
	}
}
