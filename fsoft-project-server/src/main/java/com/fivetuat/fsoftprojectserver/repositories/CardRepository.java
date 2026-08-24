package com.fivetuat.fsoftprojectserver.repositories;

import com.fivetuat.fsoftprojectserver.entities.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByDeckId(Long deckId);
    boolean existsByWordAndDeckId(String word, Long deckId);
    Page<Card> findByDeckIdOrderByPositionAsc(Long deckId, Pageable pageable);

    List<Card> findByDeckIdOrderByPositionAsc(Long deckId);
    List<Card> findByDeckIdIn(List<Long> deckIds);

    @Query(
            value = "select coalesce(max(position), 0) from cards where deck_id = :deckId",
            nativeQuery = true)
    Integer findMaxPositionByDeckId(@Param("deckId") Long deckId);

    @Query("SELECT c FROM Card c JOIN FETCH c.deck WHERE c.updatedAt >= :since ORDER BY c.updatedAt ASC, c.id ASC")
    Page<Card> findCardsChangedSince(@Param("since") Instant since, Pageable pageable);

    @Query("SELECT c.id FROM Card c ORDER BY c.id ASC")
    Page<Long> findAllCardIds(Pageable pageable);
}