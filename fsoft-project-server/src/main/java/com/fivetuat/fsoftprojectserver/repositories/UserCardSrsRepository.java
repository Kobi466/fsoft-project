package com.fivetuat.fsoftprojectserver.repositories;

import com.fivetuat.fsoftprojectserver.entities.UserCardSrs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserCardSrsRepository extends JpaRepository<UserCardSrs, Long> {
    List<UserCardSrs> findByProfileId(UUID profileId);
}