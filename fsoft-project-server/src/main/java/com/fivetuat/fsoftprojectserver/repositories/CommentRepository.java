package com.fivetuat.fsoftprojectserver.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fivetuat.fsoftprojectserver.entities.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findByIdAndIsDeletedFalse(Long id);

    List<Comment> findByIsDeletedTrueAndUpdatedAtBefore(LocalDateTime cutoffTime);

    List<Comment> findAllByCommunityPostId(Long communityPostId);

    @Query("SELECT c FROM Comment c WHERE c.communityPost.id = :postId AND c.parentComment IS NULL AND c.isDeleted = false ORDER BY c.createdAt ASC")
    Page<Comment> findRootCommentsByPostId(@Param("postId") Long postId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentId AND c.isDeleted = false ORDER BY c.createdAt ASC")
    Page<Comment> findRepliesByParentId(@Param("parentId") Long parentId, Pageable pageable);

    long countByParentCommentIdAndIsDeletedFalse(Long parentCommentId);

    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentId AND c.isDeleted = false")
    List<Comment> findAllActiveRepliesByParentId(@Param("parentId") Long parentId);

    long countByCommunityPostIdAndIsDeletedFalse(Long communityPostId);
}
