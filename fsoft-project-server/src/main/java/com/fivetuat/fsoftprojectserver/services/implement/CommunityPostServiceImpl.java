package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.CommentCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CommunityPostCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CommunityPostUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CommentResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.CommunityPostResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.PostLikeToggleResponse;
import com.fivetuat.fsoftprojectserver.entities.*;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityPostServiceImpl implements CommunityPostService {

    CommunityPostRepository communityPostRepository;
    CommunityPostMapper communityPostMapper;
    DeckRepository deckRepository;
    TagRepository tagRepository;
    PostLikeRepository postLikeRepository;
    CommentRepository commentRepository;
    CommentMapper commentMapper;
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
        Page<CommunityPost> postPage = communityPostRepository.findByIsDeletedFalseOrderByCreatedAtDesc(pageable);
        return mapToPageResponseWithLikedFlag(postPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommunityPostResponse> getPopularPosts(int page, int size) {
        Pageable pageable = PageUtils.createPageable(page, size);
        Page<CommunityPost> postPage = communityPostRepository.findByIsDeletedFalseOrderByLikeCountDescCreatedAtDesc(pageable);
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
        Page<CommunityPost> postPage = communityPostRepository.findByProfileIdAndIsDeletedFalseOrderByCreatedAtDesc(profileId, pageable);
        return mapToPageResponseWithLikedFlag(postPage);
    }

    // --- TƯƠNG TÁC THẢ TIM (TOGGLE LIKE: BẬT / TẮT THÍCH) ---

    @Override
    @Transactional
    public PostLikeToggleResponse toggleLike(Long postId) {
        Profile currentProfile = authService.getCurrentProfile();
        CommunityPost post = communityPostRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_POST_NOT_FOUND));

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

        // Xóa mềm comment cha
        comment.setDeleted(true);
        commentRepository.save(comment);

        // Xóa mềm toàn bộ các câu trả lời con (Cascade soft delete)
        List<Comment> childReplies = commentRepository.findAllActiveRepliesByParentId(commentId);
        if (!childReplies.isEmpty()) {
            childReplies.forEach(reply -> reply.setDeleted(true));
            commentRepository.saveAll(childReplies);
        }

        // Giảm số lượng comment của bài viết tương ứng với số lượng comment bị xóa (cha + các con)
        int totalDeleted = 1 + childReplies.size();
        CommunityPost post = comment.getCommunityPost();
        if (post.getCommentCount() > 0) {
            post.setCommentCount(Math.max(0, post.getCommentCount() - totalDeleted));
            communityPostRepository.save(post);
        }

        log.info("Profile {} deleted comment ID: {} and {} child replies", currentProfile.getId(), commentId, childReplies.size());
    }

    // --- HÀM HỖ TRỢ XÁC ĐỊNH LIKED_BY_CURRENT_USER CHO FEED ---

    private PageResponse<CommunityPostResponse> mapToPageResponseWithLikedFlag(Page<CommunityPost> postPage) {
        List<CommunityPost> posts = postPage.getContent();
        if (posts.isEmpty()) {
            return PageUtils.toPageResponse(postPage, communityPostMapper::toCommunityPostResponse);
        }

        // Lấy danh sách ID các bài viết mà user hiện tại đã like (nếu đã đăng nhập)
        Set<Long> likedPostIds = new HashSet<>();
        SecurityUtils.getCurrentUserLogin().ifPresent(currentUserId -> {
            List<Long> postIds = posts.stream().map(CommunityPost::getId).toList();
            likedPostIds.addAll(postLikeRepository.findLikedPostIdsByProfileIdAndPostIdsIn(currentUserId, postIds));
        });

        return PageUtils.toPageResponse(postPage, post -> {
            CommunityPostResponse response = communityPostMapper.toCommunityPostResponse(post);
            response.setLikedByCurrentUser(likedPostIds.contains(post.getId()));
            return response;
        });
    }
}
