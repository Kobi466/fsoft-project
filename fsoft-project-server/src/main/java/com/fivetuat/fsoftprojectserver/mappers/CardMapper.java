package com.fivetuat.fsoftprojectserver.mappers;

import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CardUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CardResponse;
import com.fivetuat.fsoftprojectserver.entities.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CardMapper {

    Card toCard(CardCreationRequest request);

    @Mapping(target = "deckId", source = "deck.id")
    CardResponse toCardResponse(Card card);

    void updateCard(@MappingTarget Card card, CardUpdateRequest request);
}