package com.fivetuat.fsoftprojectserver.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "admin_ai_audit_logs", indexes = {
        @Index(name = "idx_ai_audit_admin", columnList = "admin_id, created_at")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminAiAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "admin_id", nullable = false)
    UUID adminId;

    @Column(nullable = false, length = 64)
    String action;

    @Column(columnDefinition = "TEXT")
    String params;

    @Column(nullable = false, length = 16)
    String status;

    @Column(name = "result_summary", columnDefinition = "TEXT")
    String resultSummary;

    @Column(name = "reference_id")
    String referenceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}