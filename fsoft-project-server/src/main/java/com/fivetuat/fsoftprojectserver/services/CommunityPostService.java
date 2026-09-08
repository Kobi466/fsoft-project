package com.fivetuat.fsoftprojectserver.services;

import java.util.UUID;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.CommentCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CommunityPostCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CommunityPostUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.PostModerateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CommentResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.CommunityPostResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.PostLikeToggleResponse;

import org.springframework.web.multipart.MultipartFile;

public interface CommunityPostService {

    CommunityPostResponse createPost(CommunityPostCreateRequest request);

    CommunityPostResponse createPost(CommunityPostCreateRequest request, MultipartFile file);

    CommunityPostResponse getPostById(Long postId);

    CommunityPostResponse updatePost(Long postId, CommunityPostUpdateRequest request);

    CommunityPostResponse updatePost(Long postId, CommunityPostUpdateRequest request, MultipartFile file);

    void deletePost(Long postId);

    PageResponse<CommunityPostResponse> getLatestPosts(int page, int size);

    PageResponse<CommunityPostResponse> getPopularPosts(int page, int size);

    PageResponse<CommunityPostResponse> searchPosts(String keyword, Long tagId, int page, int size);

    PageResponse<CommunityPostResponse> getPostsByUser(UUID profileId, int page, int size);

    PageResponse<CommunityPostResponse> getMyPosts(int page, int size);

    PageResponse<CommunityPostResponse> getPendingPosts(int page, int size);

    CommunityPostResponse moderatePost(Long postId, PostModerateRequest request);

    PostLikeToggleResponse toggleLike(Long postId);

    CommentResponse createComment(Long postId, CommentCreateRequest request);

    CommentResponse createComment(Long postId, CommentCreateRequest request, MultipartFile file);

    PageResponse<CommentResponse> getCommentsByPostId(Long postId, int page, int size);

    PageResponse<CommentResponse> getRepliesByCommentId(Long commentId, int page, int size);

    void deleteComment(Long commentId);
}
