package com.fivetuat.fsoftprojectserver.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fivetuat.fsoftprojectserver.entities.Deck;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
    Optional<Deck> findByIdAndIsDeletedFalse(Long id);

    Optional<Deck> findByIdAndProfileIdAndIsDeletedFalse(Long id, UUID profileId);

    Page<Deck> findByProfileIdAndIsDeletedFalse(UUID profileId, Pageable pageable);
    Page<Deck> findByIsDeletedFalse(Pageable pageable);
    Page<Deck> findByTagsSlugAndIsDeletedFalseAndVisibility(String slug, DeckVisibility visibility, Pageable pageable);
}
