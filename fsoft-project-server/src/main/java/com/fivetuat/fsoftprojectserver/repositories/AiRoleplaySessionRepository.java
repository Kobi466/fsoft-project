package com.fivetuat.fsoftprojectserver.repositories;

import com.fivetuat.fsoftprojectserver.entities.AiRoleplaySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiRoleplaySessionRepository extends JpaRepository<AiRoleplaySession, String> {

    List<AiRoleplaySession> findByProfileIdOrderByUpdatedAtDesc(UUID profileId);

    Optional<AiRoleplaySession> findByIdAndProfileId(String id, UUID profileId);

    void deleteByIdAndProfileId(String id, UUID profileId);
}
