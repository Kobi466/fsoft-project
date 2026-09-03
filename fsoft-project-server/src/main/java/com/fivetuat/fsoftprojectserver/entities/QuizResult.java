package com.fivetuat.fsoftprojectserver.entities;

import com.fivetuat.fsoftprojectserver.enums.QuizScope;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_results")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    QuizScope scope;

    @Column(name = "total_questions", nullable = false)
    int totalQuestions;

    @Column(name = "correct_answers", nullable = false)
    int correctAnswers;

    @Column(nullable = false)
    float score;

    @Column(name = "accuracy_rate", nullable = false)
    float accuracyRate;

    @Column(name = "time_spent_seconds", nullable = false)
    int timeSpentSeconds;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
