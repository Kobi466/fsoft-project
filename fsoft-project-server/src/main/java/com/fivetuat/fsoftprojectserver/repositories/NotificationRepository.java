package com.fivetuat.fsoftprojectserver.repositories;

import com.fivetuat.fsoftprojectserver.entities.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);
    long countByRecipientIdAndIsReadFalse(UUID recipientId);
    List<Notification> findByRecipientIdAndIsReadFalse(UUID recipientId);
}
