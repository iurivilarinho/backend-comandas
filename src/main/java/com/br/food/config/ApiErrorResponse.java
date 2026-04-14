package com.br.food.config;

import java.time.LocalDateTime;
import java.util.List;

public class ApiErrorResponse {

	private final LocalDateTime timestamp;
	private final List<String> message;

	public ApiErrorResponse(List<String> message) {
		this.timestamp = LocalDateTime.now();
		this.message = message;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public List<String> getMessage() {
		return message;
	}
}
