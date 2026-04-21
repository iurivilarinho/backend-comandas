package com.br.food.request;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.br.food.enums.Types.OrderChannel;
import com.br.food.enums.Types.PaymentMethod;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Order payload")
public class OrderRequest {

	@NotNull(message = "Customer id is required.")
	private Long customerId;

	@NotNull(message = "Table number is required.")
	private String tableNumber;

	@NotNull(message = "Order channel is required.")
	private OrderChannel channel;

	@DecimalMin(value = "0.0", message = "Discount percentage must be at least 0.")
	@DecimalMax(value = "100.0", message = "Discount percentage must be at most 100.")
	private BigDecimal discountPercentage;

	private Boolean applyServiceFee;

	private PaymentMethod paymentMethod;

	@NotEmpty(message = "At least one order item is required.")
	@Valid
	private List<OrderItemRequest> items = new ArrayList<>();

	public Long getCustomerId() {
		return customerId;
	}

	public String getTableNumber() {
		return tableNumber;
	}

	public OrderChannel getChannel() {
		return channel;
	}

	public BigDecimal getDiscountPercentage() {
		return discountPercentage;
	}

	public Boolean getApplyServiceFee() {
		return applyServiceFee;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public List<OrderItemRequest> getItems() {
		return items;
	}
}
