package com.br.food.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "entity_name", nullable = false, length = 50)
	private String entityName;

	@Column(name = "entity_id", nullable = false)
	private Long entityId;

	@Column(name = "action_name", nullable = false, length = 100)
	private String actionName;

	@Column(name = "actor_name", nullable = false, length = 100)
	private String actorName;

	@Column(name = "details", length = 1000)
	private String details;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	public AuditLog() {
	}

	public AuditLog(String entityName, Long entityId, String actionName, String actorName, String details) {
		this.entityName = entityName;
		this.entityId = entityId;
		this.actionName = actionName;
		this.actorName = actorName;
		this.details = details;
	}

	@PrePersist
	private void prePersist() {
		if (this.createdAt == null) {
			this.createdAt = LocalDateTime.now();
		}
	}
}
