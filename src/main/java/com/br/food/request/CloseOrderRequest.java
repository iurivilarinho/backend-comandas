package com.br.food.request;

import com.br.food.enums.Types.PaymentMethod;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload used to close an order")
public class CloseOrderRequest {

	@NotNull(message = "Payment method is required.")
	private PaymentMethod paymentMethod;

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}
}
