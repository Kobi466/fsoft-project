package com.fivetuat.fsoftprojectserver.entities;

import java.time.Instant;

import com.fivetuat.fsoftprojectserver.entities.authentication.Auth;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "game_records", indexes = {
        @Index(name = "idx_game_deck_score", columnList = "deck_id, game_type, score DESC")
})
public class GameRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    Auth user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    Deck deck;

    @Column(name = "game_type", nullable = false)
    String gameType;

    int score;
    Integer maxCombo;
    Integer correctAnswers;
    Integer totalQuestions;
    Double completionTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
