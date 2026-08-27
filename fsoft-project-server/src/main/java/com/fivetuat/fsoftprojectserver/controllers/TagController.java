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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Tag Controller", description = "Quản lý nhãn (Tags) dùng để phân loại chủ đề của các bộ thẻ (ví dụ: TOEIC, JLPT, IT...)")
public class TagController {

    TagService tagService;

    @PostMapping
    @Operation(
        summary = "Tạo một Tag phân loại mới", 
        description = "Thêm một Nhãn chủ đề mới (ví dụ: Từ vựng IELTS) vào hệ thống để có thể gán vào các Bộ thẻ (Decks)."
    )
    public ApiResponse<TagResponse> createTag(
        @Parameter(description = "Dữ liệu khởi tạo Tag (Tên, màu sắc...)") @RequestBody @Valid TagRequest request
    ) {
        return ApiResponse.ok(tagService.createTag(request), SuccessCode.CREATE_TAG_SUCCESS);
    }

    @GetMapping
    @Operation(
        summary = "Lấy danh sách tất cả các Tag", 
        description = "Truy xuất danh sách tất cả các Nhãn chủ đề hiện có để làm bộ lọc tìm kiếm hoặc để người dùng lựa chọn khi tạo bộ bài."
    )
    public ApiResponse<List<TagResponse>> getAllTags() {
        return ApiResponse.ok(tagService.getAllTags(), SuccessCode.GET_TAGS_SUCCESS);
    }

    // @PutMapping("/{id}")
    // @Operation(
    //     summary = "Chỉnh sửa thông tin Tag", 
    //     description = "Đổi tên hoặc đổi định dạng hiển thị của một Tag đã tồn tại theo ID tương ứng."
    // )
    // public ApiResponse<TagResponse> updateTag(
    //         @Parameter(description = "ID của Nhãn chủ đề cần sửa") @PathVariable Long id,
    //         @Parameter(description = "Thông tin mới của Nhãn") @RequestBody @Valid TagRequest request
    // ) {
    //     return ApiResponse.ok(tagService.updateTag(id, request), SuccessCode.UPDATE_TAG_SUCCESS);
    // }

    // @DeleteMapping("/{id}")
    // @Operation(
    //     summary = "Xóa vĩnh viễn một Tag", 
    //     description = "Gỡ bỏ Tag này khỏi hệ thống. Những bộ bài đang sử dụng Tag này sẽ bị xóa liên kết tương ứng với nó."
    // )
    // public ApiResponse<Void> deleteTag(
    //     @Parameter(description = "ID của Nhãn chủ đề cần xóa") @PathVariable Long id
    // ) {
    //     tagService.deleteTag(id);
    //     return ApiResponse.ok(null, SuccessCode.DELETE_TAG_SUCCESS);
    // }
}
