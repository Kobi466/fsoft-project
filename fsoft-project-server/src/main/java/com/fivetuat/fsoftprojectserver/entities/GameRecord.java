package com.fivetuat.fsoftprojectserver.entities;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.fivetuat.fsoftprojectserver.entities.authentication.Auth;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
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
    String gameType; // "AUDIO_REFLEX"

    @Column(name = "score", nullable = false)
    Integer score;

    @Column(name = "max_combo")
    Integer maxCombo;

    @Column(name = "correct_answers")
    Integer correctAnswers;

    @Column(name = "total_questions")
    Integer totalQuestions;

    @Column(name = "completion_time")
    Double completionTime;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;
}