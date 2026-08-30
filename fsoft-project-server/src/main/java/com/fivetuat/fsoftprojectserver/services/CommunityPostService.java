package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.CommunityPostCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CommunityPostUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CommunityPostResponse;

import java.util.UUID;

public interface CommunityPostService {

    CommunityPostResponse createPost(CommunityPostCreateRequest request);

    CommunityPostResponse getPostById(Long postId);

    CommunityPostResponse updatePost(Long postId, CommunityPostUpdateRequest request);

    void deletePost(Long postId);

    PageResponse<CommunityPostResponse> getLatestPosts(int page, int size);

    PageResponse<CommunityPostResponse> getPopularPosts(int page, int size);

    PageResponse<CommunityPostResponse> searchPosts(String keyword, Long tagId, int page, int size);

    PageResponse<CommunityPostResponse> getPostsByUser(UUID profileId, int page, int size);
}
