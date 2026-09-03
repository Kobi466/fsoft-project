package com.fivetuat.fsoftprojectserver.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_activities", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_activities_profile_date", columnNames = {"profile_id", "activity_date"})
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    Profile profile;

    @Column(name = "activity_date", nullable = false)
    LocalDate activityDate;

    @Column(name = "cards_reviewed_count", nullable = false)
    int cardsReviewedCount;

    @Column(name = "quizzes_completed_count", nullable = false)
    int quizzesCompletedCount;

    @Column(name = "games_played_count", nullable = false)
    int gamesPlayedCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
