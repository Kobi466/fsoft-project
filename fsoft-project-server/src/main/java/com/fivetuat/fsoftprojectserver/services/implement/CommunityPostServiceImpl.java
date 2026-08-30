package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.CommunityPostCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CommunityPostUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CommunityPostResponse;
import com.fivetuat.fsoftprojectserver.entities.CommunityPost;
import com.fivetuat.fsoftprojectserver.entities.Deck;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.entities.Tag;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.CommunityPostMapper;
import com.fivetuat.fsoftprojectserver.repositories.CommunityPostRepository;
import com.fivetuat.fsoftprojectserver.repositories.DeckRepository;
import com.fivetuat.fsoftprojectserver.repositories.TagRepository;
import com.fivetuat.fsoftprojectserver.services.AuthService;
import com.fivetuat.fsoftprojectserver.services.CommunityPostService;
import com.fivetuat.fsoftprojectserver.utils.PageUtils;
import com.fivetuat.fsoftprojectserver.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityPostServiceImpl implements CommunityPostService {

    CommunityPostRepository communityPostRepository;
    CommunityPostMapper communityPostMapper;
    DeckRepository deckRepository;
    TagRepository tagRepository;
    AuthService authService;

    @Override
    @Transactional
    public CommunityPostResponse createPost(CommunityPostCreateRequest request) {
        Profile currentProfile = authService.getCurrentProfile();
        log.info("Creating community post by profile: {}", currentProfile.getId());

        CommunityPost post = communityPostMapper.toCommunityPost(request);
        post.setProfile(currentProfile);

        // Đính kèm bộ thẻ (nếu có)
        if (request.getDeckId() != null) {
            Deck deck = deckRepository.findByIdAndIsDeletedFalse(request.getDeckId())
                    .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));

            // Chỉ cho phép đính kèm bộ thẻ PUBLIC hoặc do chính người dùng sở hữu
            if (deck.getVisibility() != DeckVisibility.PUBLIC && !deck.getProfile().getId().equals(currentProfile.getId())) {
                throw new AppException(ErrorCode.DECK_NOT_PUBLIC);
            }
            post.setDeck(deck);
        }

        // Gắn nhãn (nếu có)
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            post.setTags(tags);
        }

        CommunityPost savedPost = communityPostRepository.save(post);
        return communityPostMapper.toCommunityPostResponse(savedPost);
    }

    @Override
    @Transactional(readOnly = true)
    public CommunityPostResponse getPostById(Long postId) {
        CommunityPost post = communityPostRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_POST_NOT_FOUND));

        return communityPostMapper.toCommunityPostResponse(post);
    }

    @Override
    @Transactional
    public CommunityPostResponse updatePost(Long postId, CommunityPostUpdateRequest request) {
        Profile currentProfile = authService.getCurrentProfile();
        CommunityPost post = communityPostRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_POST_NOT_FOUND));

        // Kiểm tra quyền tác giả
        if (!post.getProfile().getId().equals(currentProfile.getId())) {
            throw new AppException(ErrorCode.COMMUNITY_POST_ACCESS_DENIED);
        }

        communityPostMapper.updateCommunityPost(post, request);

        // Cập nhật bộ thẻ đính kèm
        if (request.getDeckId() != null) {
            Deck deck = deckRepository.findByIdAndIsDeletedFalse(request.getDeckId())
                    .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));
            if (deck.getVisibility() != DeckVisibility.PUBLIC && !deck.getProfile().getId().equals(currentProfile.getId())) {
                throw new AppException(ErrorCode.DECK_NOT_PUBLIC);
            }
            post.setDeck(deck);
        }

        // Cập nhật nhãn
        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            post.setTags(tags);
        }

        CommunityPost updatedPost = communityPostRepository.save(post);
        return communityPostMapper.toCommunityPostResponse(updatedPost);
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        Profile currentProfile = authService.getCurrentProfile();
        CommunityPost post = communityPostRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_POST_NOT_FOUND));

        // Cho phép tác giả hoặc ADMIN xóa
        boolean isAdmin = SecurityUtils.isCurrentUserInRole("ROLE_ADMIN");
        if (!post.getProfile().getId().equals(currentProfile.getId()) && !isAdmin) {
            throw new AppException(ErrorCode.COMMUNITY_POST_ACCESS_DENIED);
        }

        post.setDeleted(true);
        communityPostRepository.save(post);
        log.info("Soft-deleted community post ID: {}", postId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommunityPostResponse> getLatestPosts(int page, int size) {
        Pageable pageable = PageUtils.createPageable(page, size);
        Page<CommunityPost> postPage = communityPostRepository.findByIsDeletedFalseOrderByCreatedAtDesc(pageable);
        return PageUtils.toPageResponse(postPage, communityPostMapper::toCommunityPostResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommunityPostResponse> getPopularPosts(int page, int size) {
        Pageable pageable = PageUtils.createPageable(page, size);
        Page<CommunityPost> postPage = communityPostRepository.findByIsDeletedFalseOrderByLikeCountDescCreatedAtDesc(pageable);
        return PageUtils.toPageResponse(postPage, communityPostMapper::toCommunityPostResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommunityPostResponse> searchPosts(String keyword, Long tagId, int page, int size) {
        Pageable pageable = PageUtils.createPageable(page, size);
        Page<CommunityPost> postPage = communityPostRepository.searchPosts(keyword, tagId, pageable);
        return PageUtils.toPageResponse(postPage, communityPostMapper::toCommunityPostResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommunityPostResponse> getPostsByUser(UUID profileId, int page, int size) {
        Pageable pageable = PageUtils.createPageable(page, size);
        Page<CommunityPost> postPage = communityPostRepository.findByProfileIdAndIsDeletedFalseOrderByCreatedAtDesc(profileId, pageable);
        return PageUtils.toPageResponse(postPage, communityPostMapper::toCommunityPostResponse);
    }
}
