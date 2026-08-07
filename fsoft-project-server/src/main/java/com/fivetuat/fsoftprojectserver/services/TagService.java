package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.request.TagRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.TagResponse;

import java.util.List;

public interface TagService {
    TagResponse createTag(TagRequest request);
    List<TagResponse> getAllTags();
    TagResponse updateTag(Long id, TagRequest request);
    void deleteTag(Long id);
}
