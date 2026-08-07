package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.request.TagCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.TagResponse;

import java.util.List;

public interface TagService {
    TagResponse createTag(TagCreateRequest request);
    List<TagResponse> getAllTags();
}
