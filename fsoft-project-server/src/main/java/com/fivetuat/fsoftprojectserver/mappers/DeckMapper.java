package com.fivetuat.fsoftprojectserver.mappers;

import com.fivetuat.fsoftprojectserver.dtos.request.DeckCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.TagResponse;
import com.fivetuat.fsoftprojectserver.entities.Deck;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {CardMapper.class})
public interface DeckMapper {
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "parentDeck", ignore = true)
    @Mapping(target = "coverImageUrl", ignore = true)
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Deck toDeck(DeckCreateRequest request);

    @Mapping(target = "profileId", source = "profile.id")
    @Mapping(target = "parentDeckId", source = "parentDeck.id")
    @Mapping(target = "tags", source = "tags")
    DeckResponse toDeckResponse(Deck deck);

    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "parentDeck", ignore = true)
    @Mapping(target = "coverImageUrl", ignore = true)
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "tags", ignore = true)
    void updateDeck(@MappingTarget Deck deck, DeckUpdateRequest request);

    default Set<TagResponse> mapTags(java.util.Set<com.fivetuat.fsoftprojectserver.entities.Tag> tags) {
        if (tags == null) return null;
        return tags.stream()
                .map(tag -> com.fivetuat.fsoftprojectserver.dtos.response.TagResponse.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .build())
                .collect(java.util.stream.Collectors.toSet());
    }
}
