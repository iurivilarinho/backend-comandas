package com.br.food.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.food.models.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
