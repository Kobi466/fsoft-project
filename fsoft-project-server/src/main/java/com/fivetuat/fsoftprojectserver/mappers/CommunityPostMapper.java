package com.fivetuat.fsoftprojectserver.mappers;

import com.fivetuat.fsoftprojectserver.dtos.request.CommunityPostCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CommunityPostUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CommunityPostResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.TagResponse;
import com.fivetuat.fsoftprojectserver.entities.CommunityPost;
import com.fivetuat.fsoftprojectserver.entities.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CommunityPostMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "deck", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "attachmentUrl", ignore = true)
    @Mapping(target = "attachmentName", ignore = true)
    CommunityPost toCommunityPost(CommunityPostCreateRequest request);

    @Mapping(target = "profileId", source = "profile.id")
    @Mapping(target = "profileName", source = "profile.fullName")
    @Mapping(target = "profileAvatar", source = "profile.avatar")
    @Mapping(target = "deckId", source = "deck.id")
    @Mapping(target = "deckTitle", source = "deck.title")
    @Mapping(target = "deckDescription", source = "deck.description")
    @Mapping(target = "deckCoverImageUrl", source = "deck.coverImageUrl")
    @Mapping(target = "deckTotalCards", source = "deck.totalCards")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "likedByCurrentUser", ignore = true)
    CommunityPostResponse toCommunityPostResponse(CommunityPost post);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "deck", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "attachmentUrl", ignore = true)
    @Mapping(target = "attachmentName", ignore = true)
    void updateCommunityPost(@MappingTarget CommunityPost post, CommunityPostUpdateRequest request);

    default Set<TagResponse> mapTags(Set<Tag> tags) {
        if (tags == null) return null;
        return tags.stream()
                .map(tag -> TagResponse.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .build())
                .collect(Collectors.toSet());
    }
}
