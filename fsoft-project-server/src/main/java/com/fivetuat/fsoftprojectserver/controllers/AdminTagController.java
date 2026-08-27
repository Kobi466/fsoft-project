package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.TagRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.TagResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/tags")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin Tag Controller", description = "Quản lý nhãn / tags (Admin)")
public class AdminTagController {

    TagService tagService;

    @PostMapping
    @Operation(summary = "Tạo mới một Tag (Admin)")
    public ApiResponse<TagResponse> createTag(
            @Parameter(description = "Thông tin Tag mới") @RequestBody @Valid TagRequest request
    ) {
        return ApiResponse.ok(tagService.createTag(request), SuccessCode.CREATE_TAG_SUCCESS);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả các Tag (Admin)")
    public ApiResponse<List<TagResponse>> getAllTags() {
        return ApiResponse.ok(tagService.getAllTags(), SuccessCode.GET_TAGS_SUCCESS);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Chỉnh sửa thông tin Tag theo ID (Admin)")
    public ApiResponse<TagResponse> updateTag(
            @Parameter(description = "ID của Tag cần sửa") @PathVariable Long id,
            @Parameter(description = "Thông tin mới của Tag") @RequestBody @Valid TagRequest request
    ) {
        return ApiResponse.ok(tagService.updateTag(id, request), SuccessCode.UPDATE_TAG_SUCCESS);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa vĩnh viễn một Tag theo ID (Admin)")
    public ApiResponse<Void> deleteTag(
            @Parameter(description = "ID của Tag cần xóa") @PathVariable Long id
    ) {
        tagService.deleteTag(id);
        return ApiResponse.ok(null, SuccessCode.DELETE_TAG_SUCCESS);
    }
}
