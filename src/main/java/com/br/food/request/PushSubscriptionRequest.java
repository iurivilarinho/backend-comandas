package com.br.food.request;

import jakarta.validation.constraints.NotBlank;

public class PushSubscriptionRequest {

	@NotBlank(message = "Endpoint is required.")
	private String endpoint;

	@NotBlank(message = "p256dh key is required.")
	private String p256dh;

	@NotBlank(message = "auth key is required.")
	private String auth;

	@NotBlank(message = "Topic is required.")
	private String topic;

	private Long customerId;

	public String getEndpoint() {
		return endpoint;
	}

	public String getP256dh() {
		return p256dh;
	}

	public String getAuth() {
		return auth;
	}

	public String getTopic() {
		return topic;
	}

	public Long getCustomerId() {
		return customerId;
	}
}
