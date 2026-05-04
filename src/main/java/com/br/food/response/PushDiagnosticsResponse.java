package com.br.food.response;

import java.util.List;
import java.util.Map;

public class PushDiagnosticsResponse {

	private final boolean vapidConfigured;
	private final String vapidSubject;
	private final String vapidPublicKeyPrefix;
	private final Map<String, Long> subscriptionsByTopic;
	private final CurrentDevice currentDevice;

	public PushDiagnosticsResponse(boolean vapidConfigured,
			String vapidSubject,
			String vapidPublicKeyPrefix,
			Map<String, Long> subscriptionsByTopic,
			CurrentDevice currentDevice) {
		this.vapidConfigured = vapidConfigured;
		this.vapidSubject = vapidSubject;
		this.vapidPublicKeyPrefix = vapidPublicKeyPrefix;
		this.subscriptionsByTopic = subscriptionsByTopic;
		this.currentDevice = currentDevice;
	}

	public boolean isVapidConfigured() {
		return vapidConfigured;
	}

	public String getVapidSubject() {
		return vapidSubject;
	}

	public String getVapidPublicKeyPrefix() {
		return vapidPublicKeyPrefix;
	}

	public Map<String, Long> getSubscriptionsByTopic() {
		return subscriptionsByTopic;
	}

	public CurrentDevice getCurrentDevice() {
		return currentDevice;
	}

	public static class CurrentDevice {

		private final String endpointHost;
		private final List<TopicEntry> topics;

		public CurrentDevice(String endpointHost, List<TopicEntry> topics) {
			this.endpointHost = endpointHost;
			this.topics = topics;
		}

		public String getEndpointHost() {
			return endpointHost;
		}

		public List<TopicEntry> getTopics() {
			return topics;
		}
	}

	public static class TopicEntry {

		private final Long id;
		private final String topic;
		private final Long customerId;

		public TopicEntry(Long id, String topic, Long customerId) {
			this.id = id;
			this.topic = topic;
			this.customerId = customerId;
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
	}
}
