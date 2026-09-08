package com.fivetuat.fsoftprojectserver.repositories;

import com.fivetuat.fsoftprojectserver.entities.AdminAiAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminAiAuditLogRepository extends JpaRepository<AdminAiAuditLog, Long> {
    Page<AdminAiAuditLog> findByAdminIdOrderByCreatedAtDesc(UUID adminId, Pageable pageable);
}