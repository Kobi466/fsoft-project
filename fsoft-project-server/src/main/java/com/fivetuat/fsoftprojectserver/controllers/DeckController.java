package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckShareResponse;
import com.fivetuat.fsoftprojectserver.enums.SharePermission;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckVisibilityRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.ShareDeckRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CrammingQueueResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.DeckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@RestController
@RequestMapping("/decks")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Deck Controller", description = "Quản lý tạo, sửa, xóa, tìm kiếm và chia sẻ Bộ thẻ (Decks)")
public class DeckController {
    DeckService deckService;

    @Operation(summary = "Tạo mới bộ thẻ", description = "Tạo một bộ thẻ trống mới, cho phép đính kèm ảnh bìa đại diện (coverImage).")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            encoding = @Encoding(
                name = "request",
                contentType = MediaType.APPLICATION_JSON_VALUE
            )
        )
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DeckResponse> createDeck(
            @RequestPart(value = "request") @Valid DeckCreateRequest request,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {
        return ApiResponse.ok(deckService.createDeck(request, coverImage), SuccessCode.CREATE_DECK_SUCCESS);
    }

    @Operation(summary = "Lấy chi tiết Bộ thẻ theo ID", description = "Lấy mọi thông tin như tên, mô tả, quyền riêng tư, số lượng thẻ, v.v.. của bộ bài.")
    @GetMapping("/{id}")
    public ApiResponse<DeckResponse> getDeck(@PathVariable Long id) {
        return ApiResponse.ok(deckService.getDeck(id), SuccessCode.GET_DECK_SUCCESS);
    }

    @Operation(summary = "Cập nhật Bộ thẻ", description = "Cập nhật tên, mô tả, ảnh bìa cho một bộ bài đã tạo. " +
            "Lưu ý về visibility: USER yêu cầu PUBLIC trên bộ bài chưa phải Official sẽ đưa bộ bài vào hàng chờ duyệt của Admin (pendingPublic = true, bộ bài vẫn PRIVATE). " +
            "Bộ bài Official của USER hoặc bất kỳ thao tác nào của ADMIN sẽ áp dụng visibility trực tiếp. " +
            "Trường isOfficial trong request bị bỏ qua đối với USER (chỉ ADMIN mới được đặt official).")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(encoding = @Encoding(
                name = "request",
                contentType = MediaType.APPLICATION_JSON_VALUE
            )
        )
    )

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DeckResponse> updateDeck(
            @PathVariable Long id,
            @RequestPart("request") @Valid DeckUpdateRequest request,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {
        return ApiResponse.ok(deckService.updateDeck(id, request, coverImage), SuccessCode.UPDATE_DECK_SUCCESS);
    }

    @Operation(summary = "Xóa Bộ thẻ", description = "Xóa vĩnh viễn một bộ bài và toàn bộ thẻ bài (flashcards) bên trong nó.")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDeck(@PathVariable Long id) {
        deckService.deleteDeck(id);
        return ApiResponse.ok(null, SuccessCode.DELETE_DECK_SUCCESS);
    }

    @Operation(summary = "Lấy danh sách các bộ thẻ của tôi", description = "Truy xuất danh sách tất cả các bộ bài do tài khoản hiện tại sở hữu.")
    @GetMapping("/my")
    public ApiResponse<PageResponse<DeckResponse>> getMyDecks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(deckService.getMyDecks(page, size), SuccessCode.GET_DECKS_SUCCESS);
    }

    @Operation(summary = "Lấy danh sách bộ thẻ công khai (Khám phá)", description = "Lấy danh sách các bộ bài được cấu hình PUBLIC để người dùng khác có thể khám phá. Hỗ trợ search theo keyword, tag, ngôn ngữ.")
    @GetMapping("/public")
    public ApiResponse<PageResponse<DeckResponse>> getPublicDecks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String sourceLang,
            @RequestParam(required = false) String targetLang
    ) {
        return ApiResponse.ok(
                deckService.getPublicDecks(page, size, keyword, tagId, sourceLang, targetLang),
                SuccessCode.GET_DECKS_SUCCESS
        );
    }

    @Operation(summary = "Lấy danh sách bộ thẻ công khai của một người dùng", description = "Truy xuất tất cả các bộ thẻ ở chế độ PUBLIC của một người dùng cụ thể, hỗ trợ phân trang.")
    @GetMapping("/public/user/{profileId}")
    public ApiResponse<PageResponse<DeckResponse>> getPublicDecksByUserId(
            @Parameter(description = "ID hồ sơ cá nhân của người dùng") @PathVariable UUID profileId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(
                deckService.getPublicDecksByUserId(profileId, page, size),
                SuccessCode.GET_DECKS_SUCCESS
        );
    }

    @Operation(summary = "Lọc bộ thẻ theo trạng thái Visibility", description = "Ví dụ lấy toàn bộ các bộ thẻ PRIVATE của mình hoặc PUBLIC của mình.")
    @GetMapping("/status")
    public ApiResponse<PageResponse<DeckResponse>> getDecksByStatus(
            @RequestParam(defaultValue = "PUBLIC") DeckVisibility status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(
                deckService.getDecksByStatus(status, page, size),
                SuccessCode.GET_DECKS_SUCCESS
        );
    }

    @Operation(summary = "Đổi quyền riêng tư của bộ thẻ (Công khai / Riêng tư)", description = "Chuyển một bộ bài từ PRIVATE sang PUBLIC (để chia sẻ cộng đồng) hoặc ngược lại. " +
            "Khi yêu cầu PUBLIC: bộ bài vào hàng chờ duyệt của Admin (pendingPublic = true), vẫn PRIVATE đến khi được duyệt. " +
            "Bộ bài fork không thể yêu cầu PUBLIC (FORKED_DECK_CANNOT_BE_PUBLIC). " +
            "Yêu cầu trùng lặp khi đang chờ duyệt sẽ bị từ chối (DECK_ALREADY_PENDING_PUBLIC). " +
            "Khi bị Admin từ chối, lý do sẽ được trả về trong trường rejectionReason của DeckResponse.")
    @PutMapping("/{id}/visibility")
    public ApiResponse<Void> convertDeckVisibility(
            @PathVariable Long id,
            @RequestBody DeckVisibilityRequest request) {
        deckService.convertDeckVisibility(id, request.getVisibility());
        return ApiResponse.ok(null, SuccessCode.DECK_VISIBILITY_UPDATED);
    }

    @Operation(summary = "Sao chép (Fork) bộ thẻ công khai về tài khoản của mình", description = "Tạo một bản sao độc lập của một bộ thẻ PUBLIC (của người khác) vào bộ sưu tập cá nhân của người dùng.")
    @PostMapping("/{id}/fork")
    public ApiResponse<DeckResponse> forkDeck(@PathVariable Long id) {
        DeckResponse response = deckService.fork(id);
        return ApiResponse.ok(response, SuccessCode.DECK_FORKED);
    }

    @Operation(summary = "Chia sẻ bộ thẻ cho người dùng cụ thể", description = "Cấp quyền cho một User khác (thông qua email) được phép VIEW hoặc EDIT bộ bài của mình.")
    @PostMapping("/{id}/share")
    public ApiResponse<Void> shareDeck(
            @PathVariable Long id,
            @RequestBody @Valid ShareDeckRequest request) {
        deckService.share(id, request);
        return ApiResponse.ok(null, SuccessCode.DECK_SHARED);
    }

    @Operation(summary = "Danh sách những người được chia sẻ bộ bài", description = "Chỉ chủ sở hữu bộ bài mới có quyền xem danh sách những tài khoản đã được cấp quyền truy cập.")
    @GetMapping("/{id}/shares")
    public ApiResponse<PageResponse<DeckShareResponse>> getSharedUsers(@PathVariable Long id, Pageable pageable) {
        return ApiResponse.ok(deckService.getSharedUsers(id, pageable), SuccessCode.GET_SHARED_USERS_SUCCESS);
    }

    @Operation(summary = "Đổi quyền hạn của người được chia sẻ", description = "Thay đổi cấp độ (VIEWER / EDITOR) của một người cụ thể trong danh sách đã chia sẻ.")
    @PutMapping("/{id}/shares/{profileId}")
    public ApiResponse<Void> updateSharePermission(
            @PathVariable Long id,
            @PathVariable UUID profileId,
            @RequestParam SharePermission permission
    ) {
        deckService.updateSharePermission(id, profileId, permission);
        return ApiResponse.ok(null, SuccessCode.UPDATE_ROLE_DECK_SUCCESS);
    }

    @Operation(summary = "Xóa quyền truy cập chia sẻ của một người", description = "Tước quyền truy cập (thu hồi) của người đã được chia sẻ trước đó.")
    @DeleteMapping("/{id}/shares/{profileId}")
    public ApiResponse<Void> revokeShare(
            @PathVariable Long id,
            @PathVariable UUID profileId
    ) {
        deckService.revokeShare(id, profileId);
        return ApiResponse.ok(null, SuccessCode.UPDATE_ROLE_DECK_SUCCESS);
    }

    @Operation(summary = "Bật / Tắt chia sẻ qua đường dẫn bí mật (Link)", description = "Bật chức năng Link Sharing. Bất kỳ ai có đường Link bí mật (shareToken) đều có thể xem bộ bài.")
    @PostMapping("/{id}/share-link/toggle")
    public ApiResponse<DeckResponse> toggleShareLink(@PathVariable Long id) {
        DeckResponse response = deckService.toggleShareLink(id);
        return ApiResponse.ok(response, SuccessCode.UPDATE_DECK_SUCCESS);
    }

    @Operation(summary = "Xem bộ bài thông qua Link Share bí mật", description = "Dành cho khách hoặc bạn bè sử dụng mã shareToken (trong URL) để tải dữ liệu bộ bài.")
    @GetMapping("/shared/{shareToken}")
    public ApiResponse<DeckResponse> getDeckByShareToken(@PathVariable String shareToken) {
        DeckResponse response = deckService.getDeckByShareToken(shareToken);
        return ApiResponse.ok(response, SuccessCode.GET_DECK_SUCCESS);
    }

    @Operation(summary = "Lấy danh sách các bộ thẻ mà người khác chia sẻ cho tôi", description = "Liệt kê danh sách các bộ bài mà tài khoản hiện tại được người khác cấp quyền truy cập.")
    @GetMapping("/shared-with-me")
    public ApiResponse<PageResponse<DeckResponse>> getSharedWithMeDecks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(deckService.getSharedWithMeDecks(page, size), SuccessCode.GET_DECKS_SUCCESS);
    }

    @Operation(summary = "Lấy danh sách thẻ bài ôn tập cấp tốc (Cramming Mode) trước kỳ thi", 
               description = "Lấy toàn bộ từ vựng trong bộ bài để lướt xem nhanh trước kỳ thi. HOÀN TOÀN KHÔNG làm thay đổi tiến trình SRS/FSRS.")
    @GetMapping("/{deckId}/cram-cards")
    public ApiResponse<CrammingQueueResponse> getCramCards(
            @Parameter(description = "ID của bộ thẻ cần ôn cấp tốc") @PathVariable Long deckId,
            @Parameter(description = "Có tráo đổi thứ tự thẻ ngẫu nhiên hay không (default: false)") @RequestParam(required = false, defaultValue = "false") Boolean shuffle,
            @Parameter(description = "Bộ lọc từ vựng: ALL, LEARNED, UNLEARNED (default: ALL)") @RequestParam(required = false, defaultValue = "ALL") String filter,
            @Parameter(description = "Số lượng thẻ tối đa lấy ra (0 = lấy tất cả)") @RequestParam(required = false, defaultValue = "0") Integer limit
    ) {
        return ApiResponse.ok(deckService.getCramCards(deckId, shuffle, filter, limit), SuccessCode.GET_CRAMMING_QUEUE_SUCCESS);
    }
}
