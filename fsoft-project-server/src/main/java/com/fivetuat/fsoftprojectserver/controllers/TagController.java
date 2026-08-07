package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.TagCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.TagResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.TagService;
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
public class TagController {
    TagService tagService;

    @PostMapping
    public ApiResponse<TagResponse> createTag(@RequestBody @Valid TagCreateRequest request) {
        return ApiResponse.ok(tagService.createTag(request), SuccessCode.CREATE_TAG_SUCCESS);
    }

    @GetMapping
    public ApiResponse<List<TagResponse>> getAllTags() {
        return ApiResponse.ok(tagService.getAllTags(), SuccessCode.GET_TAGS_SUCCESS);
    }
}
