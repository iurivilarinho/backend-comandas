package com.br.food.request;

import com.br.food.enums.Types.PaymentMethod;

import jakarta.validation.constraints.Size;

public class RequestOrderCheckoutRequest {

	private PaymentMethod paymentMethod;

	@Size(max = 255, message = "Notes must have at most 255 characters.")
	private String notes;

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public String getNotes() {
		return notes;
	}
}
