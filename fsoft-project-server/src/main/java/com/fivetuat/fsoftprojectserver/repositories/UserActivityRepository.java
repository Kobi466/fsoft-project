package com.fivetuat.fsoftprojectserver.repositories;

import com.fivetuat.fsoftprojectserver.entities.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    List<UserActivity> findByProfileIdAndActivityDateBetween(UUID profileId, LocalDate from, LocalDate to);

    Optional<UserActivity> findByProfileIdAndActivityDate(UUID profileId, LocalDate activityDate);

    List<UserActivity> findAllByProfileIdAndActivityDate(UUID profileId, LocalDate activityDate);
}