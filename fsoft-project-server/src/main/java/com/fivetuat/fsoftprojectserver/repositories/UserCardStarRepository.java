package com.fivetuat.fsoftprojectserver.repositories;

import com.fivetuat.fsoftprojectserver.entities.Card;
import com.fivetuat.fsoftprojectserver.entities.UserCardStar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCardStarRepository extends JpaRepository<UserCardStar, Long> {

    Optional<UserCardStar> findByProfileIdAndCardId(UUID profileId, Long cardId);

    @Query("""
        SELECT ucs.card 
        FROM UserCardStar ucs 
        WHERE ucs.profile.id = :profileId 
          AND (:deckId IS NULL OR ucs.card.deck.id = :deckId)
        ORDER BY ucs.createdAt DESC
    """)
    Page<Card> findStarredCardsByProfileId(
            @Param("profileId") UUID profileId,
            @Param("deckId") Long deckId,
            Pageable pageable
    );
}
