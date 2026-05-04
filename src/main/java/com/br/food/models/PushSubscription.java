package com.br.food.models;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "push_subscription",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_push_subscription_endpoint_topic",
				columnNames = { "endpoint", "topic" }),
		indexes = {
				@Index(name = "idx_push_subscription_topic", columnList = "topic"),
				@Index(name = "idx_push_subscription_customer", columnList = "customer_id"),
				@Index(name = "idx_push_subscription_endpoint", columnList = "endpoint"),
		})
public class PushSubscription {

	public static final String TOPIC_KITCHEN = "kitchen";
	public static final String TOPIC_CUSTOMER = "customer";
	public static final String TOPIC_TABLES = "tables";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "endpoint", nullable = false, length = 1024)
	private String endpoint;

	@Column(name = "p256dh", nullable = false, length = 255)
	private String p256dh;

	@Column(name = "auth_key", nullable = false, length = 255)
	private String auth;

	@Column(name = "topic", nullable = false, length = 32)
	private String topic;

	@Column(name = "customer_id")
	private Long customerId;

	@Column(name = "created_at", updatable = false, nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public PushSubscription() {
	}

	public PushSubscription(String endpoint, String p256dh, String auth, String topic, Long customerId) {
		this.endpoint = endpoint;
		this.p256dh = p256dh;
		this.auth = auth;
		this.topic = topic;
		this.customerId = customerId;
	}

	public void update(String p256dh, String auth, String topic, Long customerId) {
		this.p256dh = p256dh;
		this.auth = auth;
		this.topic = topic;
		this.customerId = customerId;
	}

	@PrePersist
	private void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	private void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

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

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof PushSubscription other)) {
			return false;
		}
		return id != null && Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
