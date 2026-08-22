package com.fivetuat.fsoftprojectserver.repositories;

import com.fivetuat.fsoftprojectserver.entities.Card;
import com.fivetuat.fsoftprojectserver.entities.UserCardSrs;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserCardSrsRepository extends JpaRepository<UserCardSrs, Long> {
    Optional<UserCardSrs> findByProfileIdAndCardId(UUID profileId, Long cardId);

    List<UserCardSrs> findByProfileIdAndCardDeckId(UUID profileId, Long deckId);

    @Query(
            value = """
            select s.card.id
                from UserCardSrs s
                where s.profile.id = :profileId
                  and s.card.deck.id = :deckId
            """
    )
    Set<Long> findLearnedCardIdsByProfileAndDeckId(
            @Param("profileId") UUID profileId,
            @Param("deckId") Long deckId
    );

    void deleteByProfileIdAndCardDeckId(UUID profileId, Long deckId);


    @Query("""
        select c from Card c
            where c.deck.id = :deckId
                and c.id not in (
                    select s.card.id
                        from UserCardSrs s
                        where s.profile.id = :profileId
                    )
            order by c.position asc
    """)
    List<Card> findUnlearnedCardsByDeckAndProfile(
            @Param("profileId") UUID profileId,
            @Param("deckId") Long deckId,
            Pageable pageable
    );

    List<UserCardSrs> findByProfileIdAndCardDeckIdAndNextReviewDateLessThanEqualOrderByNextReviewDateAsc(UUID userId, Long deckId, LocalDateTime now);

    List<UserCardSrs> findByProfileIdAndNextReviewDateLessThanEqualOrderByNextReviewDateAsc(UUID userId, LocalDateTime now);
}