package com.fivetuat.fsoftprojectserver.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fivetuat.fsoftprojectserver.entities.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    @Query("SELECT c FROM ChatMessage c " +
            "JOIN FETCH c.sender s " +
            "LEFT JOIN FETCH s.profile " +
            "LEFT JOIN FETCH c.replyTo r " +
            "LEFT JOIN FETCH r.sender rs " +
            "LEFT JOIN FETCH rs.profile " +
            "ORDER BY c.createdAt DESC")
    Page<ChatMessage> findRecentMessages(Pageable pageable);
}
