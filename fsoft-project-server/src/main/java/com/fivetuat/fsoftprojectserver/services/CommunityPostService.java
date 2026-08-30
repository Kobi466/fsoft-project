package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.CommentCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CommunityPostCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CommunityPostUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CommentResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.CommunityPostResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.PostLikeToggleResponse;

import java.util.UUID;

public interface CommunityPostService {

    // --- Quản lý Bài viết & Feed ---
    CommunityPostResponse createPost(CommunityPostCreateRequest request);

    CommunityPostResponse getPostById(Long postId);

    CommunityPostResponse updatePost(Long postId, CommunityPostUpdateRequest request);

    void deletePost(Long postId);

    PageResponse<CommunityPostResponse> getLatestPosts(int page, int size);

    PageResponse<CommunityPostResponse> getPopularPosts(int page, int size);

    PageResponse<CommunityPostResponse> searchPosts(String keyword, Long tagId, int page, int size);

    PageResponse<CommunityPostResponse> getPostsByUser(UUID profileId, int page, int size);

    // --- Tương tác Thả tim Toggle (Bật / Tắt Like) ---
    PostLikeToggleResponse toggleLike(Long postId);

    // --- Bình luận (Comments) ---
    CommentResponse createComment(Long postId, CommentCreateRequest request);

    // Lấy danh sách bình luận gốc (kèm số lượng replyCount)
    PageResponse<CommentResponse> getCommentsByPostId(Long postId, int page, int size);

    // Lấy danh sách câu trả lời con khi người dùng bấm "Xem phản hồi"
    PageResponse<CommentResponse> getRepliesByCommentId(Long commentId, int page, int size);

    void deleteComment(Long commentId);
}
