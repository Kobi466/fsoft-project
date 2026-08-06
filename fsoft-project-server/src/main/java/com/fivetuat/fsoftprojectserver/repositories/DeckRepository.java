package com.fivetuat.fsoftprojectserver.repositories;

import com.fivetuat.fsoftprojectserver.entities.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
    Optional<Deck> findByIdAndIsDeletedFalse(Long id);
    List<Deck> findByProfileIdAndIsDeletedFalse(UUID profileId);
    List<Deck> findByIsDeletedFalse();
}
