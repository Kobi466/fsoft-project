package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.CommentCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CommunityPostCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CommunityPostUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CommentResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.CommunityPostResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.PostLikeToggleResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.CommunityPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/community-posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Community Post Controller", description = "Quản lý bài viết, bảng tin, thả tim và bình luận trong cộng đồng")
public class CommunityPostController {

    CommunityPostService communityPostService;

    // --- QUẢN LÝ BÀI VIẾT ---

    @PostMapping
    @Operation(
            summary = "Tạo bài viết mới trong cộng đồng",
            description = "Tạo một bài viết mới với nội dung (Markdown), có thể đính kèm một bộ thẻ Deck công khai (tùy chọn) và gắn nhiều nhãn Tags (tùy chọn)."
    )
    public ApiResponse<CommunityPostResponse> createPost(
            @Valid @RequestBody CommunityPostCreateRequest request
    ) {
        return ApiResponse.ok(communityPostService.createPost(request), SuccessCode.CREATE_COMMUNITY_POST_SUCCESS);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Xem chi tiết một bài viết",
            description = "Lấy toàn bộ thông tin chi tiết của bài viết theo ID (nội dung, thông tin tác giả, bộ thẻ đính kèm, danh sách tag, số like, số comment, trạng thái đã like chưa)."
    )
    public ApiResponse<CommunityPostResponse> getPostById(
            @Parameter(description = "ID của bài viết") @PathVariable Long id
    ) {
        return ApiResponse.ok(communityPostService.getPostById(id), SuccessCode.GET_COMMUNITY_POST_SUCCESS);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Cập nhật bài viết",
            description = "Chỉnh sửa tiêu đề, nội dung, bộ thẻ đính kèm hoặc danh sách nhãn của bài viết. Chỉ tác giả bài viết mới có quyền cập nhật."
    )
    public ApiResponse<CommunityPostResponse> updatePost(
            @Parameter(description = "ID của bài viết cần sửa") @PathVariable Long id,
            @Valid @RequestBody CommunityPostUpdateRequest request
    ) {
        return ApiResponse.ok(communityPostService.updatePost(id, request), SuccessCode.UPDATE_COMMUNITY_POST_SUCCESS);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Xóa bài viết",
            description = "Xóa (soft-delete) bài viết khỏi cộng đồng. Chỉ tác giả hoặc Quản trị viên (ADMIN) mới có quyền xóa."
    )
    public ApiResponse<Void> deletePost(
            @Parameter(description = "ID của bài viết cần xóa") @PathVariable Long id
    ) {
        communityPostService.deletePost(id);
        return ApiResponse.ok(null, SuccessCode.DELETE_COMMUNITY_POST_SUCCESS);
    }

    // --- BẢNG TIN & TÌM KIẾM ---

    @GetMapping("/latest")
    @Operation(
            summary = "Bảng tin bài viết mới nhất",
            description = "Lấy danh sách các bài viết mới đăng nhất trong cộng đồng, hỗ trợ phân trang (page, size)."
    )
    public ApiResponse<PageResponse<CommunityPostResponse>> getLatestPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(communityPostService.getLatestPosts(page, size), SuccessCode.GET_COMMUNITY_POSTS_SUCCESS);
    }

    @GetMapping("/popular")
    @Operation(
            summary = "Bảng tin bài viết nổi bật (Nhiều like nhất)",
            description = "Lấy danh sách các bài viết được cộng đồng yêu thích nhất (sắp xếp theo lượt Like giảm dần)."
    )
    public ApiResponse<PageResponse<CommunityPostResponse>> getPopularPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(communityPostService.getPopularPosts(page, size), SuccessCode.GET_COMMUNITY_POSTS_SUCCESS);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Tìm kiếm bài viết cộng đồng",
            description = "Tìm kiếm bài viết theo từ khóa (trong tiêu đề/nội dung) và/hoặc lọc theo nhãn Tag cụ thể."
    )
    public ApiResponse<PageResponse<CommunityPostResponse>> searchPosts(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String keyword,
            @Parameter(description = "ID của nhãn tag muốn lọc") @RequestParam(required = false) Long tagId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(communityPostService.searchPosts(keyword, tagId, page, size), SuccessCode.GET_COMMUNITY_POSTS_SUCCESS);
    }

    @GetMapping("/user/{profileId}")
    @Operation(
            summary = "Lấy danh sách bài viết theo người dùng",
            description = "Truy xuất danh sách tất cả các bài viết do một người dùng cụ thể đã đăng tải."
    )
    public ApiResponse<PageResponse<CommunityPostResponse>> getPostsByUser(
            @Parameter(description = "ID hồ sơ cá nhân của người dùng") @PathVariable UUID profileId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(communityPostService.getPostsByUser(profileId, page, size), SuccessCode.GET_COMMUNITY_POSTS_SUCCESS);
    }

    // --- TƯƠNG TÁC THẢ TIM (TOGGLE LIKE) ---

    @PostMapping("/{id}/like")
    @Operation(
            summary = "Thả tim / Bỏ tim bài viết (Toggle Like)",
            description = "Bấm để thích bài viết. Nếu đã thích rồi thì bấm lại sẽ tự động bỏ thích và trả về trạng thái thích cùng số lượt thích mới nhất."
    )
    public ApiResponse<PostLikeToggleResponse> toggleLike(
            @Parameter(description = "ID của bài viết") @PathVariable Long id
    ) {
        return ApiResponse.ok(communityPostService.toggleLike(id), SuccessCode.LIKE_POST_SUCCESS);
    }

    // --- BÌNH LUẬN (COMMENTS) ---

    @PostMapping("/{id}/comments")
    @Operation(
            summary = "Gửi bình luận vào bài viết",
            description = "Đăng bình luận mới cho bài viết. Nếu muốn trả lời (Reply) một bình luận khác, hãy truyền thêm parentCommentId."
    )
    public ApiResponse<CommentResponse> createComment(
            @Parameter(description = "ID của bài viết") @PathVariable Long id,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        return ApiResponse.ok(communityPostService.createComment(id, request), SuccessCode.CREATE_COMMENT_SUCCESS);
    }

    @GetMapping("/{id}/comments")
    @Operation(
            summary = "Lấy danh sách bình luận của bài viết",
            description = "Lấy danh sách các bình luận gốc kèm theo các câu trả lời con (Replies), hỗ trợ phân trang."
    )
    public ApiResponse<PageResponse<CommentResponse>> getCommentsByPostId(
            @Parameter(description = "ID của bài viết") @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(communityPostService.getCommentsByPostId(id, page, size), SuccessCode.GET_COMMENTS_SUCCESS);
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(
            summary = "Xóa bình luận",
            description = "Xóa bình luận. Người viết bình luận HOẶC Chủ bài viết HOẶC Quản trị viên (ADMIN) có quyền xóa."
    )
    public ApiResponse<Void> deleteComment(
            @Parameter(description = "ID của bình luận cần xóa") @PathVariable Long commentId
    ) {
        communityPostService.deleteComment(commentId);
        return ApiResponse.ok(null, SuccessCode.DELETE_COMMENT_SUCCESS);
    }
}
