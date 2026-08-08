package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.TagRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.TagResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Tag Controller", description = "Quản lý nhãn phân loại chủ đề (Tags)")
public class TagController {

    TagService tagService;

    @PostMapping
    @Operation(summary = "Tạo mới một Nhãn chủ đề")
    public ApiResponse<TagResponse> createTag(@RequestBody @Valid TagRequest request) {
        return ApiResponse.ok(tagService.createTag(request), SuccessCode.CREATE_TAG_SUCCESS);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả Nhãn chủ đề")
    public ApiResponse<List<TagResponse>> getAllTags() {
        return ApiResponse.ok(tagService.getAllTags(), SuccessCode.GET_TAGS_SUCCESS);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật Nhãn chủ đề theo ID")
    public ApiResponse<TagResponse> updateTag(
            @PathVariable Long id,
            @RequestBody @Valid TagRequest request
    ) {
        return ApiResponse.ok(tagService.updateTag(id, request), SuccessCode.UPDATE_TAG_SUCCESS);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa Nhãn chủ đề theo ID")
    public ApiResponse<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ApiResponse.ok(null, SuccessCode.DELETE_TAG_SUCCESS);
    }
}
