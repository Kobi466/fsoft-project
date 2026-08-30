package com.fivetuat.fsoftprojectserver.repositories;

import com.fivetuat.fsoftprojectserver.entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findByIdAndIsDeletedFalse(Long id);

    // 1. Lấy danh sách bình luận gốc chưa bị xóa (parentComment IS NULL)
    @Query("SELECT c FROM Comment c WHERE c.communityPost.id = :postId AND c.parentComment IS NULL AND c.isDeleted = false ORDER BY c.createdAt ASC")
    Page<Comment> findRootCommentsByPostId(@Param("postId") Long postId, Pageable pageable);

    // 2. Lấy danh sách câu trả lời con (Replies) của 1 bình luận cha cụ thể (có phân trang)
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentId AND c.isDeleted = false ORDER BY c.createdAt ASC")
    Page<Comment> findRepliesByParentId(@Param("parentId") Long parentId, Pageable pageable);

    // 3. Đếm số lượng câu trả lời con chưa bị xóa của 1 bình luận cha
    long countByParentCommentIdAndIsDeletedFalse(Long parentCommentId);

    // 4. Lấy tất cả câu trả lời con để phục vụ xóa cascade
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentId AND c.isDeleted = false")
    List<Comment> findAllActiveRepliesByParentId(@Param("parentId") Long parentId);

    long countByCommunityPostIdAndIsDeletedFalse(Long communityPostId);
}
