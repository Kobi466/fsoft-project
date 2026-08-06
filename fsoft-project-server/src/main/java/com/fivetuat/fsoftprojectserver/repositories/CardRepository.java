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

    List<Card> findByDeckIdOrderByPositionAsc(Long deckId);
    List<Card> findByDeckId(Long deckId);
    Page<Card> findByDeckIdOrderByPositionAsc(Long deckId, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.deckId = :deckId AND " +
            "(:keyword IS NULL OR LOWER(c.word) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.meaning) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.note) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Card> searchCardsByDeckId(@Param("deckId") Long deckId, @Param("keyword") String keyword, Pageable pageable);

    Optional<Card> findFirstByDeckIdOrderByPositionDesc(Long deckId);
}