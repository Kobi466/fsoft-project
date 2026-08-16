package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.CardAiResponse;
import com.fivetuat.fsoftprojectserver.entities.Card;
import com.fivetuat.fsoftprojectserver.repositories.CardRepository;
import com.fivetuat.fsoftprojectserver.services.InternalCardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalCardServiceImpl implements InternalCardService {

    CardRepository cardRepository;

    @Override
    public PageResponse<CardAiResponse> getChangedCards(Instant since, int page, int size) {
        int springPage = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(springPage, size);
        
        Page<Card> cardPage = cardRepository.findCardsChangedSince(since, pageable);
        
        List<CardAiResponse> responses = cardPage.getContent().stream()
                .map(this::mapToAiResponse)
                .collect(Collectors.toList());

        return PageResponse.<CardAiResponse>builder()
                .content(responses)
                .pageNo(page)
                .pageSize(size)
                .totalElements(cardPage.getTotalElements())
                .totalPages(cardPage.getTotalPages())
                .last(cardPage.isLast())
                .build();
    }

    @Override
    public PageResponse<Long> getCardIds(int page, int size) {
        int springPage = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(springPage, size);
        
        Page<Long> idsPage = cardRepository.findAllCardIds(pageable);
        
        return PageResponse.<Long>builder()
                .content(idsPage.getContent())
                .pageNo(page)
                .pageSize(size)
                .totalElements(idsPage.getTotalElements())
                .totalPages(idsPage.getTotalPages())
                .last(idsPage.isLast())
                .build();
    }

    private CardAiResponse mapToAiResponse(Card card) {
        return CardAiResponse.builder()
                .cardId(card.getId())
                .deckId(card.getDeck() != null ? card.getDeck().getId() : null)
                .deckTitle(card.getDeck() != null ? card.getDeck().getTitle() : null)
                .word(card.getWord())
                .phonetic(card.getPhonetic())
                .partOfSpeech(card.getPartOfSpeech())
                .meaning(card.getMeaning())
                .definitionEn(card.getDefinitionEn())
                .exampleSentence(card.getExampleSentence())
                .exampleMeaning(card.getExampleMeaning())
                .audioUrl(card.getAudioUrl())
                .note(card.getNote())
                .updatedAt(card.getUpdatedAt())
                .build();
    }
}
