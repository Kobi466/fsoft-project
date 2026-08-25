package com.fivetuat.fsoftprojectserver.entities;

import java.time.LocalDateTime;

import com.fivetuat.fsoftprojectserver.enums.SrsStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "user_card_srs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCardSrs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    Card card;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    SrsStatus status;

    @Column(name = "easiness_factor", nullable = false)
    @Builder.Default
    float easinessFactor = 2.5f;

    @Column(name = "stability")
    Double stability;

    @Column(name = "difficulty")
    Double difficulty;

    @Column(nullable = false)
    int repetitions;

    @Column(name = "srs_interval", nullable = false) // Note: interval is a SQL keyword in some DBs, renaming to srs_interval to be safe!
    int interval;

    @Column(name = "last_reviewed_at")
    LocalDateTime lastReviewedAt;

    @Column(name = "next_review_date", nullable = false)
    LocalDateTime nextReviewDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
