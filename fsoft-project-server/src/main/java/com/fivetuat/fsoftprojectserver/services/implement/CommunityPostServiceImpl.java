package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.CommentCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CommunityPostCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CommunityPostUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.PostModerateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CommentResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.CommunityPostResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.PostLikeToggleResponse;
import com.fivetuat.fsoftprojectserver.entities.*;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.enums.PostStatus;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.CommentMapper;
import com.fivetuat.fsoftprojectserver.mappers.CommunityPostMapper;
import com.fivetuat.fsoftprojectserver.repositories.*;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    CommunityPostMapper communityPostMapper;
    CommentMapper commentMapper;

    // --- QUẢN LÝ BÀI VIẾT & FEED ---

    @Override
    @Transactional
    public CommunityPostResponse createPost(CommunityPostCreateRequest request) {
        Profile currentProfile = authService.getCurrentProfile();

        CommunityPost post = communityPostMapper.toCommunityPost(request);
        post.setProfile(currentProfile);

        // Nếu là ADMIN đăng bài -> Tự động duyệt luôn; Nếu là USER -> Chờ duyệt (PENDING)
        boolean isAdmin = SecurityUtils.isCurrentUserInRole("ROLE_ADMIN");
        if (isAdmin) {
            post.setStatus(PostStatus.APPROVED);
            post.setApprovedBy(currentProfile);
            post.setApprovedAt(LocalDateTime.now());
        } else {
            post.setStatus(PostStatus.PENDING);
        }

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
        log.info("Created community post ID: {} with status: {}", savedPost.getId(), savedPost.getStatus());
        return communityPostMapper.toCommunityPostResponse(savedPost);
    }

    @Override
    @Transactional(readOnly = true)
    public CommunityPostResponse getPostById(Long postId) {
        CommunityPost post = communityPostRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_POST_NOT_FOUND));

        // Nếu bài viết chưa được duyệt, chỉ tác giả bài viết hoặc ADMIN mới có quyền xem
        if (post.getStatus() != PostStatus.APPROVED) {
            UUID currentUserId = SecurityUtils.getCurrentUserLogin().orElse(null);
            boolean isAuthor = currentUserId != null && post.getProfile().getId().equals(currentUserId);
            boolean isAdmin = SecurityUtils.isCurrentUserInRole("ROLE_ADMIN");
            if (!isAuthor && !isAdmin) {
                throw new AppException(ErrorCode.COMMUNITY_POST_NOT_FOUND);
            }
        }

        CommunityPostResponse response = communityPostMapper.toCommunityPostResponse(post);

        // Kiểm tra xem người dùng hiện tại đã thả tim bài viết này chưa
        SecurityUtils.getCurrentUserLogin().ifPresent(currentUserId -> {
            boolean liked = postLikeRepository.existsByCommunityPostIdAndProfileId(postId, currentUserId);
            response.setLikedByCurrentUser(liked);
        });

        return response;
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
        Page<CommunityPost> postPage = communityPostRepository.findByStatusAndIsDeletedFalseOrderByCreatedAtDesc(PostStatus.APPROVED, pageable);
        return mapToPageResponseWithLikedFlag(postPage);
    }

    @Override
    @Transactional(readOnly = true)
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

    // --- KIỂM DUYỆT BÀI VIẾT (ADMIN MODERATION) ---

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
        return communityPostMapper.toCommunityPostResponse(savedPost);
    }

    // --- TƯƠNG TÁC THẢ TIM (TOGGLE LIKE: BẬT / TẮT THÍCH) ---

    @Override
    @Transactional
    public PostLikeToggleResponse toggleLike(Long postId) {
        Profile currentProfile = authService.getCurrentProfile();
        CommunityPost post = communityPostRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_POST_NOT_FOUND));

        // Chỉ cho phép like bài viết đã được duyệt (APPROVED)
        if (post.getStatus() != PostStatus.APPROVED) {
            throw new AppException(ErrorCode.POST_NOT_APPROVED);
        }

        Optional<PostLike> existingLike = postLikeRepository.findByCommunityPostIdAndProfileId(postId, currentProfile.getId());

        boolean isLiked;
        if (existingLike.isPresent()) {
            // Đã like rồi -> Bỏ like (Unlike)
            postLikeRepository.delete(existingLike.get());
            if (post.getLikeCount() > 0) {
                post.setLikeCount(post.getLikeCount() - 1);
            }
            isLiked = false;
            log.info("Profile {} unliked post ID: {}", currentProfile.getId(), postId);
        } else {
            // Chưa like -> Thêm like
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

        return PostLikeToggleResponse.builder()
                .postId(postId)
                .liked(isLiked)
                .likeCount(savedPost.getLikeCount())
                .build();
    }

    // --- BÌNH LUẬN (COMMENTS) ---

    @Override
    @Transactional
    public CommentResponse createComment(Long postId, CommentCreateRequest request) {
        Profile currentProfile = authService.getCurrentProfile();
        CommunityPost post = communityPostRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_POST_NOT_FOUND));

        // Chỉ cho phép bình luận bài viết đã được duyệt (APPROVED)
        if (post.getStatus() != PostStatus.APPROVED) {
            throw new AppException(ErrorCode.POST_NOT_APPROVED);
        }

        Comment comment = commentMapper.toComment(request);
        comment.setCommunityPost(post);
        comment.setProfile(currentProfile);

        // Nếu là trả lời (Reply) một bình luận khác
        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findByIdAndIsDeletedFalse(request.getParentCommentId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

            if (!parentComment.getCommunityPost().getId().equals(postId)) {
                throw new AppException(ErrorCode.VALIDATION_ERROR);
            }
            comment.setParentComment(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);

        // Tăng số lượng comment của bài viết
        post.setCommentCount(post.getCommentCount() + 1);
        communityPostRepository.save(post);
        log.info("Profile {} commented on post ID: {}", currentProfile.getId(), postId);

        return commentMapper.toCommentResponse(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getCommentsByPostId(Long postId, int page, int size) {
        // Kiểm tra bài viết có tồn tại không
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
        // Kiểm tra bình luận cha có tồn tại không
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

        // Kiểm tra quyền: Người viết comment HOẶC Chủ bài viết HOẶC ADMIN có quyền xóa
        boolean isCommentAuthor = comment.getProfile().getId().equals(currentProfile.getId());
        boolean isPostAuthor = comment.getCommunityPost().getProfile().getId().equals(currentProfile.getId());
        boolean isAdmin = SecurityUtils.isCurrentUserInRole("ROLE_ADMIN");

        if (!isCommentAuthor && !isPostAuthor && !isAdmin) {
            throw new AppException(ErrorCode.COMMENT_ACCESS_DENIED);
        }

        // Soft delete bình luận này
        comment.setDeleted(true);
        commentRepository.save(comment);

        // Cascade xóa tất cả các bình luận con (Replies) bên dưới nó
        List<Comment> childReplies = commentRepository.findAllActiveRepliesByParentId(commentId);
        int totalDeletedComments = 1;
        if (!childReplies.isEmpty()) {
            for (Comment reply : childReplies) {
                reply.setDeleted(true);
            }
            commentRepository.saveAll(childReplies);
            totalDeletedComments += childReplies.size();
        }

        // Cập nhật lại commentCount trên bài viết
        CommunityPost post = comment.getCommunityPost();
        int newCommentCount = Math.max(0, post.getCommentCount() - totalDeletedComments);
        post.setCommentCount(newCommentCount);
        communityPostRepository.save(post);

        log.info("Soft-deleted comment ID: {} and {} replies on post ID: {}", commentId, childReplies.size(), post.getId());
    }

    // --- HELPER FUNCTION ---

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
}
