package com.fivetuat.fsoftprojectserver.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fivetuat.fsoftprojectserver.entities.CommunityPost;
import com.fivetuat.fsoftprojectserver.enums.PostStatus;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    Optional<CommunityPost> findByIdAndIsDeletedFalse(Long id);
    Page<CommunityPost> findByStatusAndIsDeletedFalseOrderByCreatedAtDesc(PostStatus status, Pageable pageable);

    Page<CommunityPost> findByStatusAndIsDeletedFalseOrderByLikeCountDescCreatedAtDesc(PostStatus status, Pageable pageable);

    Page<CommunityPost> findByProfileIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(UUID profileId, PostStatus status, Pageable pageable);

    long countByProfileIdAndStatusAndIsDeletedFalse(UUID profileId, PostStatus status);

    Page<CommunityPost> findByProfileIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID profileId, Pageable pageable);

    Page<CommunityPost> findByStatusAndIsDeletedFalseOrderByCreatedAtAsc(PostStatus status, Pageable pageable);

    @Query("SELECT p FROM CommunityPost p WHERE p.isDeleted = false " +
            "AND p.status = com.fivetuat.fsoftprojectserver.enums.PostStatus.APPROVED " +
            "AND (CAST(:keyword AS string) IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) " +
            "OR LOWER(p.content) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) " +
            "AND (:tagId IS NULL OR EXISTS (SELECT 1 FROM p.tags t WHERE t.id = :tagId)) " +
            "ORDER BY p.createdAt DESC")
    Page<CommunityPost> searchPosts(
            @Param("keyword") String keyword,
            @Param("tagId") Long tagId,
            Pageable pageable
    );
}
