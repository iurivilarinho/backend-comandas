package com.br.food.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.models.AuditLog;
import com.br.food.repository.AuditLogRepository;

@Service
public class AuditLogService {

	private final AuditLogRepository auditLogRepository;

	public AuditLogService(AuditLogRepository auditLogRepository) {
		this.auditLogRepository = auditLogRepository;
	}

	@Transactional
	public void register(String entityName, Long entityId, String actionName, String actorName, String details) {
		auditLogRepository.save(new AuditLog(entityName, entityId, actionName, safeActor(actorName), details));
	}

	private String safeActor(String actorName) {
		return actorName == null || actorName.isBlank() ? "system" : actorName.trim();
	}
}
