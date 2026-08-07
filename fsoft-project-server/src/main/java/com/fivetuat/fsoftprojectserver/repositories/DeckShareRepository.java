package com.fivetuat.fsoftprojectserver.repositories;

import com.fivetuat.fsoftprojectserver.entities.DeckShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeckShareRepository extends JpaRepository<DeckShare, Long> {
    boolean existsByDeckIdAndProfileId(Long deckId, UUID profileId);
    Optional<DeckShare> findByDeckIdAndProfileId(Long deckId, UUID profileId);
}
