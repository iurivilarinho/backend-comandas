package com.br.food.response;

import java.util.Map;

public class PushDiagnosticsResponse {

	private final boolean vapidConfigured;
	private final Map<String, Long> subscriptionsByTopic;
	private final SubscriptionInfo currentDevice;

	public PushDiagnosticsResponse(boolean vapidConfigured,
			Map<String, Long> subscriptionsByTopic,
			SubscriptionInfo currentDevice) {
		this.vapidConfigured = vapidConfigured;
		this.subscriptionsByTopic = subscriptionsByTopic;
		this.currentDevice = currentDevice;
	}

	public boolean isVapidConfigured() {
		return vapidConfigured;
	}

	public Map<String, Long> getSubscriptionsByTopic() {
		return subscriptionsByTopic;
	}

	public SubscriptionInfo getCurrentDevice() {
		return currentDevice;
	}

	public static class SubscriptionInfo {

		private final Long id;
		private final String topic;
		private final Long customerId;
		private final String endpointHost;

		public SubscriptionInfo(Long id, String topic, Long customerId, String endpointHost) {
			this.id = id;
			this.topic = topic;
			this.customerId = customerId;
			this.endpointHost = endpointHost;
		}

		public Long getId() {
			return id;
		}

		public String getTopic() {
			return topic;
		}

		public Long getCustomerId() {
			return customerId;
		}

		public String getEndpointHost() {
			return endpointHost;
		}
	}
}
