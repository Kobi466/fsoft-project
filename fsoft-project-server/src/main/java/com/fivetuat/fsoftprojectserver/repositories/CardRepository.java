package com.fivetuat.fsoftprojectserver.repositories;

import com.fivetuat.fsoftprojectserver.entities.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findByDeckIdOrderByPositionAsc(Long deckId, Pageable pageable);

    @Query(
            value = "select coalesce(max(position), 0) from cards where deck_id = :deckId",
            nativeQuery = true)
    Integer findMaxPositionByDeckId(@Param("deckId") Long deckId);
}