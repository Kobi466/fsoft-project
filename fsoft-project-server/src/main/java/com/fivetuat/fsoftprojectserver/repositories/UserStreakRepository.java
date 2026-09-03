package com.fivetuat.fsoftprojectserver.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fivetuat.fsoftprojectserver.dtos.response.ReminderUserResponse;
import com.fivetuat.fsoftprojectserver.entities.UserStreak;

@Repository
public interface UserStreakRepository extends JpaRepository<UserStreak, Long> {
    Optional<UserStreak> findByProfileId(UUID profileId);

    List<UserStreak> findAllByProfileId(UUID profileId);

    @Query("""
        SELECT new com.fivetuat.fsoftprojectserver.dtos.response.ReminderUserResponse(
            p.auth.email,
            p.fullName,
            COALESCE(s.currentStreak, 0),
            (
                (SELECT COUNT(srs) FROM UserCardSrs srs WHERE srs.profile = p AND srs.nextReviewDate <= CURRENT_TIMESTAMP)
                +
                (SELECT COUNT(c) FROM Card c WHERE c.deck.profile = p AND NOT EXISTS (SELECT 1 FROM UserCardSrs s2 WHERE s2.profile = p AND s2.card = c))
            )
        )
        FROM Profile p
        LEFT JOIN p.streak s
        WHERE (s.lastActiveDate IS NULL OR s.lastActiveDate < :today)
          AND NOT EXISTS (
              SELECT 1 FROM UserActivity a
              WHERE a.profile = p
                AND a.activityDate = :today
                AND a.cardsReviewedCount > 0
          )
    """)
    Page<ReminderUserResponse> findUsersNeedingReminder(@Param("today") LocalDate today, Pageable pageable);
}