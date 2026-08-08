package com.fivetuat.fsoftprojectserver.mappers;

import com.fivetuat.fsoftprojectserver.entities.Tag;
import org.mapstruct.Mapper;

import com.fivetuat.fsoftprojectserver.dtos.request.TagRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.TagResponse;

@Mapper(componentModel = "spring")
public interface TagMapper {
    Tag toTag(TagRequest tagRequest);
    TagResponse toTagResponse(Tag tag);
}
