package com.br.food.response;

public class PushTestResponse {

	private final boolean attempted;
	private final Integer statusCode;
	private final String errorMessage;
	private final Long subscriptionId;
	private final String topic;

	public PushTestResponse(boolean attempted, Integer statusCode, String errorMessage,
			Long subscriptionId, String topic) {
		this.attempted = attempted;
		this.statusCode = statusCode;
		this.errorMessage = errorMessage;
		this.subscriptionId = subscriptionId;
		this.topic = topic;
	}

	public boolean isAttempted() {
		return attempted;
	}

	public Integer getStatusCode() {
		return statusCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public Long getSubscriptionId() {
		return subscriptionId;
	}

	public String getTopic() {
		return topic;
	}
}
