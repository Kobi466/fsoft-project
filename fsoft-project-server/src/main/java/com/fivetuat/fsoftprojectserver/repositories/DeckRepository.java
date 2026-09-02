package com.fivetuat.fsoftprojectserver.repositories;

import com.fivetuat.fsoftprojectserver.entities.Deck;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
    Optional<Deck> findByIdAndIsDeletedFalse(Long id);

    Page<Deck> findByProfileAndIsDeletedFalse(Profile profile, Pageable pageable);

    Page<Deck> findByVisibilityAndIsDeletedFalse(DeckVisibility visibility, Pageable pageable);

    Page<Deck> findByPendingPublicTrueAndIsDeletedFalse(Pageable pageable);

    Page<Deck> findByProfileAndVisibilityAndIsDeletedFalse(Profile profile, DeckVisibility visibility, Pageable pageable);
    
    Page<Deck> findByProfileIdAndVisibilityAndIsDeletedFalse(UUID profileId, DeckVisibility visibility, Pageable pageable);

    long countByProfileIdAndVisibilityAndIsDeletedFalse(UUID profileId, DeckVisibility visibility);

    @Query("SELECT ds.deck FROM DeckShare ds WHERE ds.profile = :profile AND ds.deck.isDeleted = false")
    Page<Deck> findSharedDecksByProfile(@Param("profile") Profile profile, Pageable pageable);

    Optional<Deck> findByIdAndProfileIdAndIsDeletedFalse(Long id, UUID profileId);

    boolean existsByParentDeckIdAndProfileIdAndIsDeletedFalse(Long parentDeckId, UUID profileId);

    @Query("SELECT d FROM Deck d WHERE d.visibility = com.fivetuat.fsoftprojectserver.enums.DeckVisibility.PUBLIC AND d.isDeleted = false " +
            "AND (CAST(:keyword AS string) IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR LOWER(d.description) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) " +
            "AND (:tagId IS NULL OR EXISTS (SELECT 1 FROM d.tags t WHERE t.id = :tagId)) " +
            "AND (CAST(:sourceLang AS string) IS NULL OR LOWER(d.sourceLanguage) = LOWER(CAST(:sourceLang AS string))) " +
            "AND (CAST(:targetLang AS string) IS NULL OR LOWER(d.targetLanguage) = LOWER(CAST(:targetLang AS string)))")
    Page<Deck> searchPublicDecks(
            @Param("keyword") String keyword,
            @Param("tagId") Long tagId,
            @Param("sourceLang") String sourceLang,
            @Param("targetLang") String targetLang,
            Pageable pageable
    );

    Optional<Deck> findByShareTokenAndIsShareLinkEnabledTrueAndIsDeletedFalse(String shareToken);
    
    @Query("SELECT d.id FROM Deck d WHERE d.isDeleted = false AND (" +
           "d.profile.id = :currentUserId OR " +
           "d.visibility = com.fivetuat.fsoftprojectserver.enums.DeckVisibility.PUBLIC OR " +
           "d.id IN (SELECT ds.deck.id FROM DeckShare ds WHERE ds.profile.id = :currentUserId)" +
           ")")
    List<Long> findAllAccessibleDeckIdsByProfileId(@Param("currentUserId") UUID currentUserId);

    long countByIsOfficialTrueAndIsDeletedFalse();
    long countByVisibilityAndIsDeletedFalse(DeckVisibility visibility);
}

