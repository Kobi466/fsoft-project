package com.fivetuat.fsoftprojectserver.repositories;

import com.fivetuat.fsoftprojectserver.entities.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    @Query("SELECT m FROM ChatMessage m " +
            "LEFT JOIN FETCH m.sender s " +
            "LEFT JOIN FETCH s.profile " +
            "LEFT JOIN FETCH m.replyTo r " +
            "LEFT JOIN FETCH r.sender rs " +
            "LEFT JOIN FETCH rs.profile " +
            "ORDER BY m.createdAt DESC")
    Page<ChatMessage> findRecentMessages(Pageable pageable);
}