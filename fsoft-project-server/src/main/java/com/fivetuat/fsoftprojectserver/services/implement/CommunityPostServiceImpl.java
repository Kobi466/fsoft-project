package com.fivetuat.fsoftprojectserver.services.implement;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.CommentCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CommunityPostCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CommunityPostUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.PostModerateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CommentDeleteResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.CommentResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.CommunityEventPayload;
import com.fivetuat.fsoftprojectserver.dtos.response.CommunityPostResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.PostLikeToggleResponse;
import com.fivetuat.fsoftprojectserver.entities.Comment;
import com.fivetuat.fsoftprojectserver.entities.CommunityPost;
import com.fivetuat.fsoftprojectserver.entities.Deck;
import com.fivetuat.fsoftprojectserver.entities.PostLike;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.entities.Tag;
import com.fivetuat.fsoftprojectserver.enums.CommunityEventType;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.enums.FolderName;
import com.fivetuat.fsoftprojectserver.enums.PostStatus;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.CommentMapper;
import com.fivetuat.fsoftprojectserver.mappers.CommunityPostMapper;
import com.fivetuat.fsoftprojectserver.repositories.CommentRepository;
import com.fivetuat.fsoftprojectserver.repositories.CommunityPostRepository;
import com.fivetuat.fsoftprojectserver.repositories.DeckRepository;
import com.fivetuat.fsoftprojectserver.repositories.PostLikeRepository;
import com.fivetuat.fsoftprojectserver.repositories.TagRepository;
import com.fivetuat.fsoftprojectserver.services.AuthService;
import com.fivetuat.fsoftprojectserver.services.CommunityPostService;
import com.fivetuat.fsoftprojectserver.services.UploadService;
import com.fivetuat.fsoftprojectserver.utils.PageUtils;
import com.fivetuat.fsoftprojectserver.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityPostServiceImpl implements CommunityPostService {

    CommunityPostRepository communityPostRepository;
    PostLikeRepository postLikeRepository;
    CommentRepository commentRepository;
    DeckRepository deckRepository;
    TagRepository tagRepository;
    AuthService authService;
    UploadService uploadService;
    CommunityPostMapper communityPostMapper;
    CommentMapper commentMapper;
    SimpMessagingTemplate messagingTemplate;


    @Override
    @Transactional
    public CommunityPostResponse createPost(CommunityPostCreateRequest request) {
        return createPost(request, null);
    }

    @Override
    @Transactional
    public CommunityPostResponse createPost(CommunityPostCreateRequest request, MultipartFile file) {
        Profile currentProfile = authService.getCurrentProfile();

        CommunityPost post = communityPostMapper.toCommunityPost(request);
        post.setProfile(currentProfile);

        if (file != null && !file.isEmpty()) {
            String attachmentUrl = uploadService.uploadFile(file, FolderName.ATTACHMENTS);
            post.setAttachmentUrl(attachmentUrl);
            post.setAttachmentName(file.getOriginalFilename());
        }

        boolean isAdmin = SecurityUtils.isCurrentUserInRole("ROLE_ADMIN");
        if (isAdmin) {
            post.setStatus(PostStatus.APPROVED);
            post.setApprovedBy(currentProfile);
            post.setApprovedAt(LocalDateTime.now());
        } else {
            post.setStatus(PostStatus.PENDING);
        }

        if (request.getDeckId() != null) {
            Deck deck = deckRepository.findByIdAndIsDeletedFalse(request.getDeckId())
                    .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));

            if (deck.getVisibility() != DeckVisibility.PUBLIC && !deck.getProfile().getId().equals(currentProfile.getId())) {
                throw new AppException(ErrorCode.DECK_NOT_PUBLIC);
            }
            post.setDeck(deck);
        }

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            post.setTags(tags);
        }

        CommunityPost savedPost = communityPostRepository.save(post);
        log.info("Created community post ID: {} with status: {}", savedPost.getId(), savedPost.getStatus());
        CommunityPostResponse response = communityPostMapper.toCommunityPostResponse(savedPost);

        if (savedPost.getStatus() == PostStatus.APPROVED) {
            sendCommunityEvent("/topic/community/feed", CommunityEventPayload.<CommunityPostResponse>builder()
                    .type(CommunityEventType.NEW_POST_APPROVED)
                    .postId(savedPost.getId())
                    .data(response)
                    .build());
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CommunityPostResponse getPostById(Long postId) {
        CommunityPost post = communityPostRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_POST_NOT_FOUND));

        if (post.getStatus() != PostStatus.APPROVED) {
            UUID currentUserId = SecurityUtils.getCurrentUserLogin().orElse(null);
            boolean isAuthor = currentUserId != null && post.getProfile().getId().equals(currentUserId);
            boolean isAdmin = SecurityUtils.isCurrentUserInRole("ROLE_ADMIN");
            if (!isAuthor && !isAdmin) {
                throw new AppException(ErrorCode.COMMUNITY_POST_NOT_FOUND);
            }
        }

        CommunityPostResponse response = communityPostMapper.toCommunityPostResponse(post);

        SecurityUtils.getCurrentUserLogin().ifPresent(currentUserId -> {
            boolean liked = postLikeRepository.existsByCommunityPostIdAndProfileId(postId, currentUserId);
            response.setLikedByCurrentUser(liked);
        });

        return response;
    }

    @Override
    @Transactional
    public CommunityPostResponse updatePost(Long postId, CommunityPostUpdateRequest request) {
        return updatePost(postId, request, null);
    }

    @Override
    @Transactional
    public CommunityPostResponse updatePost(Long postId, CommunityPostUpdateRequest request, MultipartFile file) {
        Profile currentProfile = authService.getCurrentProfile();
        CommunityPost post = communityPostRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_POST_NOT_FOUND));

        if (!post.getProfile().getId().equals(currentProfile.getId())) {
            throw new AppException(ErrorCode.COMMUNITY_POST_ACCESS_DENIED);
        }

        communityPostMapper.updateCommunityPost(post, request);

        if (file != null && !file.isEmpty()) {
            String attachmentUrl = uploadService.uploadFile(file, FolderName.ATTACHMENTS);
            post.setAttachmentUrl(attachmentUrl);
            post.setAttachmentName(file.getOriginalFilename());
        }

        if (request.getDeckId() != null) {
            Deck deck = deckRepository.findByIdAndIsDeletedFalse(request.getDeckId())
                    .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));
            if (deck.getVisibility() != DeckVisibility.PUBLIC && !deck.getProfile().getId().equals(currentProfile.getId())) {
                throw new AppException(ErrorCode.DECK_NOT_PUBLIC);
            }
            post.setDeck(deck);
        }

        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            post.setTags(tags);
        }

        CommunityPost updatedPost = communityPostRepository.save(post);
        CommunityPostResponse response = communityPostMapper.toCommunityPostResponse(updatedPost);

        if (updatedPost.getStatus() == PostStatus.APPROVED) {
            CommunityEventPayload<CommunityPostResponse> updatePayload = CommunityEventPayload.<CommunityPostResponse>builder()
                    .type(CommunityEventType.POST_UPDATED)
                    .postId(postId)
                    .data(response)
                    .build();
            sendCommunityEvent("/topic/posts/" + postId, updatePayload);
            sendCommunityEvent("/topic/community/feed", updatePayload);
        }

        return response;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"community_latest_posts", "community_popular_posts", "community_post_detail"}, allEntries = true)
    public void deletePost(Long postId) {
        Profile currentProfile = authService.getCurrentProfile();
        CommunityPost post = communityPostRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_POST_NOT_FOUND));

        boolean isAdmin = SecurityUtils.isCurrentUserInRole("ROLE_ADMIN");
        if (!post.getProfile().getId().equals(currentProfile.getId()) && !isAdmin) {
            throw new AppException(ErrorCode.COMMUNITY_POST_ACCESS_DENIED);
        }

        post.setDeleted(true);
        communityPostRepository.save(post);

        List<Comment> postComments = commentRepository.findAllByCommunityPostId(postId);
        if (!postComments.isEmpty()) {
            for (Comment c : postComments) {
                c.setDeleted(true);
            }
            commentRepository.saveAll(postComments);
            log.info("Soft-deleted {} comments of post ID: {}", postComments.size(), postId);
        }

        log.info("Soft-deleted community post ID: {}", postId);

        CommunityEventPayload<Map<String, Object>> deletePayload = CommunityEventPayload.<Map<String, Object>>builder()
                .type(CommunityEventType.POST_DELETED)
                .postId(postId)
                .data(Map.of("postId", postId))
                .build();
        sendCommunityEvent("/topic/posts/" + postId, deletePayload);
        sendCommunityEvent("/topic/community/feed", deletePayload);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "community_latest_posts", key = "#page + ':' + #size")
    public PageResponse<CommunityPostResponse> getLatestPosts(int page, int size) {
        Pageable pageable = PageUtils.createPageable(page, size);
        Page<CommunityPost> postPage = communityPostRepository.findByStatusAndIsDeletedFalseOrderByCreatedAtDesc(PostStatus.APPROVED, pageable);
        return mapToPageResponseWithLikedFlag(postPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "community_popular_posts", key = "#page + ':' + #size")
    public PageResponse<CommunityPostResponse> getPopularPosts(int page, int size) {
        Pageable pageable = PageUtils.createPageable(page, size);
        Page<CommunityPost> postPage = communityPostRepository.findByStatusAndIsDeletedFalseOrderByLikeCountDescCreatedAtDesc(PostStatus.APPROVED, pageable);
        return mapToPageResponseWithLikedFlag(postPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommunityPostResponse> searchPosts(String keyword, Long tagId, int page, int size) {
        Pageable pageable = PageUtils.createPageable(page, size);
        Page<CommunityPost> postPage = communityPostRepository.searchPosts(keyword, tagId, pageable);
        return mapToPageResponseWithLikedFlag(postPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommunityPostResponse> getPostsByUser(UUID profileId, int page, int size) {
        Pageable pageable = PageUtils.createPageable(page, size);
        Page<CommunityPost> postPage = communityPostRepository.findByProfileIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(profileId, PostStatus.APPROVED, pageable);
        return mapToPageResponseWithLikedFlag(postPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommunityPostResponse> getMyPosts(int page, int size) {
        Profile currentProfile = authService.getCurrentProfile();
        Pageable pageable = PageUtils.createPageable(page, size);
        Page<CommunityPost> postPage = communityPostRepository.findByProfileIdAndIsDeletedFalseOrderByCreatedAtDesc(currentProfile.getId(), pageable);
        return mapToPageResponseWithLikedFlag(postPage);
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommunityPostResponse> getPendingPosts(int page, int size) {
        Pageable pageable = PageUtils.createPageable(page, size);
        Page<CommunityPost> postPage = communityPostRepository.findByStatusAndIsDeletedFalseOrderByCreatedAtAsc(PostStatus.PENDING, pageable);
        return mapToPageResponseWithLikedFlag(postPage);
    }

    @Override
    @Transactional
    public CommunityPostResponse moderatePost(Long postId, PostModerateRequest request) {
        Profile currentAdmin = authService.getCurrentProfile();
        CommunityPost post = communityPostRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_POST_NOT_FOUND));

        if (request.getStatus() == PostStatus.REJECTED) {
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new AppException(ErrorCode.REJECTION_REASON_REQUIRED);
            }
            post.setStatus(PostStatus.REJECTED);
            post.setRejectionReason(request.getReason().trim());
        } else if (request.getStatus() == PostStatus.APPROVED) {
            post.setStatus(PostStatus.APPROVED);
            post.setRejectionReason(null);
        } else {
            post.setStatus(PostStatus.PENDING);
        }

        post.setApprovedBy(currentAdmin);
        post.setApprovedAt(LocalDateTime.now());

        CommunityPost savedPost = communityPostRepository.save(post);
        log.info("Admin {} moderated post ID: {} to status: {}", currentAdmin.getId(), postId, request.getStatus());
        CommunityPostResponse response = communityPostMapper.toCommunityPostResponse(savedPost);

        if (savedPost.getStatus() == PostStatus.APPROVED) {
            sendCommunityEvent("/topic/community/feed", CommunityEventPayload.<CommunityPostResponse>builder()
                    .type(CommunityEventType.NEW_POST_APPROVED)
                    .postId(postId)
                    .data(response)
                    .build());
        }

        return response;
    }


    @Override
    @Transactional
    public PostLikeToggleResponse toggleLike(Long postId) {
        Profile currentProfile = authService.getCurrentProfile();
        CommunityPost post = communityPostRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_POST_NOT_FOUND));

        if (post.getStatus() != PostStatus.APPROVED) {
            throw new AppException(ErrorCode.POST_NOT_APPROVED);
        }

        Optional<PostLike> existingLike = postLikeRepository.findByCommunityPostIdAndProfileId(postId, currentProfile.getId());

        boolean isLiked;
        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            if (post.getLikeCount() > 0) {
                post.setLikeCount(post.getLikeCount() - 1);
            }
            isLiked = false;
            log.info("Profile {} unliked post ID: {}", currentProfile.getId(), postId);
        } else {
            PostLike postLike = PostLike.builder()
                    .communityPost(post)
                    .profile(currentProfile)
                    .build();
            postLikeRepository.save(postLike);
            post.setLikeCount(post.getLikeCount() + 1);
            isLiked = true;
            log.info("Profile {} liked post ID: {}", currentProfile.getId(), postId);
        }

        CommunityPost savedPost = communityPostRepository.save(post);

        PostLikeToggleResponse toggleResponse = PostLikeToggleResponse.builder()
                .postId(postId)
                .liked(isLiked)
                .likeCount(savedPost.getLikeCount())
                .build();

        CommunityEventPayload<PostLikeToggleResponse> likePayload = CommunityEventPayload.<PostLikeToggleResponse>builder()
                .type(CommunityEventType.LIKE_COUNT_CHANGED)
                .postId(postId)
                .data(toggleResponse)
                .build();

        sendCommunityEvent("/topic/posts/" + postId, likePayload);
        sendCommunityEvent("/topic/community/feed", likePayload);

        return toggleResponse;
    }

    @Override
    @Transactional
    public CommentResponse createComment(Long postId, CommentCreateRequest request) {
        return createComment(postId, request, null);
    }

    @Override
    @Transactional
    public CommentResponse createComment(Long postId, CommentCreateRequest request, MultipartFile file) {
        Profile currentProfile = authService.getCurrentProfile();
        CommunityPost post = communityPostRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_POST_NOT_FOUND));

        if (post.getStatus() != PostStatus.APPROVED) {
            throw new AppException(ErrorCode.POST_NOT_APPROVED);
        }

        Comment comment = commentMapper.toComment(request);
        comment.setCommunityPost(post);
        comment.setProfile(currentProfile);

        if (file != null && !file.isEmpty()) {
            String attachmentUrl = uploadService.uploadFile(file, FolderName.ATTACHMENTS);
            comment.setAttachmentUrl(attachmentUrl);
            comment.setAttachmentName(file.getOriginalFilename());
        }

        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findByIdAndIsDeletedFalse(request.getParentCommentId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

            if (!parentComment.getCommunityPost().getId().equals(postId)) {
                throw new AppException(ErrorCode.VALIDATION_ERROR);
            }
            comment.setParentComment(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);

        post.setCommentCount(post.getCommentCount() + 1);
        communityPostRepository.save(post);
        log.info("Profile {} commented on post ID: {}", currentProfile.getId(), postId);

        CommentResponse response = commentMapper.toCommentResponse(savedComment);
        response.setReplyCount(0);

        sendCommunityEvent("/topic/posts/" + postId, CommunityEventPayload.<CommentResponse>builder()
                .type(CommunityEventType.COMMENT_CREATED)
                .postId(postId)
                .data(response)
                .build());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getCommentsByPostId(Long postId, int page, int size) {
        communityPostRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_POST_NOT_FOUND));

        Pageable pageable = PageUtils.createPageable(page, size);
        Page<Comment> rootCommentsPage = commentRepository.findRootCommentsByPostId(postId, pageable);

        return PageUtils.toPageResponse(rootCommentsPage, rootComment -> {
            CommentResponse response = commentMapper.toCommentResponse(rootComment);
            long replyCount = commentRepository.countByParentCommentIdAndIsDeletedFalse(rootComment.getId());
            response.setReplyCount((int) replyCount);
            return response;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getRepliesByCommentId(Long commentId, int page, int size) {
        commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        Pageable pageable = PageUtils.createPageable(page, size);
        Page<Comment> repliesPage = commentRepository.findRepliesByParentId(commentId, pageable);

        return PageUtils.toPageResponse(repliesPage, reply -> {
            CommentResponse response = commentMapper.toCommentResponse(reply);
            long replyCount = commentRepository.countByParentCommentIdAndIsDeletedFalse(reply.getId());
            response.setReplyCount((int) replyCount);
            return response;
        });
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Profile currentProfile = authService.getCurrentProfile();
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        boolean isCommentAuthor = comment.getProfile().getId().equals(currentProfile.getId());
        boolean isPostAuthor = comment.getCommunityPost().getProfile().getId().equals(currentProfile.getId());
        boolean isAdmin = SecurityUtils.isCurrentUserInRole("ROLE_ADMIN");

        if (!isCommentAuthor && !isPostAuthor && !isAdmin) {
            throw new AppException(ErrorCode.COMMENT_ACCESS_DENIED);
        }

        comment.setDeleted(true);
        commentRepository.save(comment);

        List<Comment> childReplies = commentRepository.findAllActiveRepliesByParentId(commentId);
        int totalDeletedComments = 1;
        if (!childReplies.isEmpty()) {
            for (Comment reply : childReplies) {
                reply.setDeleted(true);
            }
            commentRepository.saveAll(childReplies);
            totalDeletedComments += childReplies.size();
        }

        CommunityPost post = comment.getCommunityPost();
        int newCommentCount = Math.max(0, post.getCommentCount() - totalDeletedComments);
        post.setCommentCount(newCommentCount);
        communityPostRepository.save(post);

        log.info("Soft-deleted comment ID: {} and {} replies on post ID: {}", commentId, childReplies.size(), post.getId());

        Long parentCommentId = comment.getParentComment() != null ? comment.getParentComment().getId() : null;
        Long postId = post.getId();

        CommentDeleteResponse deleteData = CommentDeleteResponse.builder()
                .commentId(commentId)
                .parentCommentId(parentCommentId)
                .build();

        sendCommunityEvent("/topic/posts/" + postId, CommunityEventPayload.<CommentDeleteResponse>builder()
                .type(CommunityEventType.COMMENT_DELETED)
                .postId(postId)
                .data(deleteData)
                .build());
    }


    private PageResponse<CommunityPostResponse> mapToPageResponseWithLikedFlag(Page<CommunityPost> postPage) {
        List<Long> postIds = postPage.getContent().stream()
                .map(CommunityPost::getId)
                .collect(Collectors.toList());

        Set<Long> likedPostIds = new HashSet<>();
        Optional<UUID> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent() && !postIds.isEmpty()) {
            likedPostIds.addAll(postLikeRepository.findLikedPostIdsByProfileIdAndPostIdsIn(currentUserLogin.get(), postIds));
        }

        return PageUtils.toPageResponse(postPage, post -> {
            CommunityPostResponse response = communityPostMapper.toCommunityPostResponse(post);
            response.setLikedByCurrentUser(likedPostIds.contains(post.getId()));
            return response;
        });
    }

    @Override
    @Transactional
    @CacheEvict(value = {"community_latest_posts", "community_popular_posts", "community_post_detail"}, allEntries = true)
    public void purgeSoftDeletedPostsAndComments() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(30);
        log.info("Bắt đầu thực hiện purge soft-deleted community posts và comments trước thời điểm: {}", cutoffTime);

        List<CommunityPost> expiredPosts = communityPostRepository.findByIsDeletedTrueAndUpdatedAtBefore(cutoffTime);
        if (!expiredPosts.isEmpty()) {
            log.info("Tìm thấy {} bài viết soft-deleted cần xóa vĩnh viễn.", expiredPosts.size());
            for (CommunityPost post : expiredPosts) {
                if (post.getAttachmentUrl() != null && !post.getAttachmentUrl().isBlank()) {
                    try {
                        uploadService.deleteFile(post.getAttachmentUrl());
                        log.info("Đã xóa file S3 của post ID {}: {}", post.getId(), post.getAttachmentUrl());
                    } catch (Exception e) {
                        log.error("Lỗi khi xóa file S3 của post ID {}: {}", post.getId(), e.getMessage());
                    }
                }

                List<Comment> postComments = commentRepository.findAllByCommunityPostId(post.getId());
                if (!postComments.isEmpty()) {
                    for (Comment comment : postComments) {
                        if (comment.getAttachmentUrl() != null && !comment.getAttachmentUrl().isBlank()) {
                            try {
                                uploadService.deleteFile(comment.getAttachmentUrl());
                                log.info("Đã xóa file S3 của comment ID {}: {}", comment.getId(), comment.getAttachmentUrl());
                            } catch (Exception e) {
                                log.error("Lỗi khi xóa file S3 của comment ID {}: {}", comment.getId(), e.getMessage());
                            }
                        }
                        comment.setParentComment(null);
                    }
                    commentRepository.saveAll(postComments);
                    commentRepository.deleteAll(postComments);
                    log.info("Đã xóa {} comments thuộc post ID {}", postComments.size(), post.getId());
                }

                postLikeRepository.deleteByCommunityPostId(post.getId());

                if (post.getTags() != null) {
                    post.getTags().clear();
                    communityPostRepository.save(post);
                }

                communityPostRepository.delete(post);
                log.info("Đã xóa vĩnh viễn post ID {}", post.getId());
            }
        }

        List<Comment> standaloneExpiredComments = commentRepository.findByIsDeletedTrueAndUpdatedAtBefore(cutoffTime);
        if (!standaloneExpiredComments.isEmpty()) {
            log.info("Tìm thấy {} comments lẻ tẻ soft-deleted cần xóa vĩnh viễn.", standaloneExpiredComments.size());
            for (Comment comment : standaloneExpiredComments) {
                if (comment.getAttachmentUrl() != null && !comment.getAttachmentUrl().isBlank()) {
                    try {
                        uploadService.deleteFile(comment.getAttachmentUrl());
                        log.info("Đã xóa file S3 của standalone comment ID {}: {}", comment.getId(), comment.getAttachmentUrl());
                    } catch (Exception e) {
                        log.error("Lỗi khi xóa file S3 của standalone comment ID {}: {}", comment.getId(), e.getMessage());
                    }
                }
                comment.setParentComment(null);
            }
            commentRepository.saveAll(standaloneExpiredComments);
            commentRepository.deleteAll(standaloneExpiredComments);
            log.info("Đã xóa vĩnh viễn {} standalone comments.", standaloneExpiredComments.size());
        }

        log.info("Hoàn tất tiến trình purge soft-deleted community posts và comments.");
    }

    private void sendCommunityEvent(String destination, CommunityEventPayload<?> payload) {
        try {
            messagingTemplate.convertAndSend(destination, payload);
            log.info("Broadcast community event [{}] to topic: {}", payload.getType(), destination);
        } catch (Exception e) {
            log.error("Failed to broadcast community event to {}: {}", destination, e.getMessage());
        }
    }
}
