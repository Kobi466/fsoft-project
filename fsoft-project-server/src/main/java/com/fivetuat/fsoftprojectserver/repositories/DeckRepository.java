package com.fivetuat.fsoftprojectserver.repositories;

import com.fivetuat.fsoftprojectserver.entities.Deck;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
    Optional<Deck> findByIdAndIsDeletedFalse(Long id);

    Page<Deck> findByProfileAndIsDeletedFalse(Profile profile, Pageable pageable);

    List<Deck> findByIsDeletedFalse();

    boolean existsByParentDeckIdAndProfileIdAndIsDeletedFalse(Long parentDeckId, UUID profileId);
}
