package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CardUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CardResponse;
import com.fivetuat.fsoftprojectserver.entities.Card;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.CardMapper;
import com.fivetuat.fsoftprojectserver.repositories.CardRepository;
import com.fivetuat.fsoftprojectserver.services.CardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CardServiceImpl implements CardService {

    CardRepository cardRepository;
    CardMapper cardMapper;

    @Override
    @Transactional
    public CardResponse createCard(CardCreationRequest request) {
        log.info("Creating card for deckId {} with word: {}", request.getDeckId(), request.getWord());

        Card card = cardMapper.toCard(request);

        // Auto-assign position within deck if not provided
        if (card.getPosition() == null) {
            Integer maxPosition = cardRepository.findFirstByDeckIdOrderByPositionDesc(request.getDeckId())
                    .map(Card::getPosition)
                    .orElse(0);
            card.setPosition(maxPosition + 1);
        }

        Card savedCard = cardRepository.save(card);
        return cardMapper.toCardResponse(savedCard);
    }

    @Override
    public CardResponse getCardById(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND));
        return cardMapper.toCardResponse(card);
    }

    @Override
    public PageResponse<CardResponse> getCardsByDeckId(Long deckId, int page, int size, String keyword, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy != null ? sortBy : "position");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Card> cardPage = (keyword != null && !keyword.trim().isEmpty())
                ? cardRepository.searchCardsByDeckId(deckId, keyword.trim(), pageable)
                : cardRepository.findByDeckIdOrderByPositionAsc(deckId, pageable);

        List<CardResponse> content = cardPage.getContent()
                .stream()
                .map(cardMapper::toCardResponse)
                .toList();

        return PageResponse.<CardResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPages(cardPage.getTotalPages())
                .totalElements(cardPage.getTotalElements())
                .content(content)
                .last(cardPage.isLast())
                .build();
    }

    @Override
    public List<CardResponse> getAllCardsInDeck(Long deckId) {
        return cardRepository.findByDeckIdOrderByPositionAsc(deckId)
                .stream()
                .map(cardMapper::toCardResponse)
                .toList();
    }

    @Override
    @Transactional
    public CardResponse updateCard(Long cardId, CardUpdateRequest request) {
        log.info("Updating card ID: {}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND));

        cardMapper.updateCard(card, request);

        Card updatedCard = cardRepository.save(card);
        return cardMapper.toCardResponse(updatedCard);
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        log.info("Deleting card ID: {}", cardId);

        if (!cardRepository.existsById(cardId)) {
            throw new AppException(ErrorCode.CARD_NOT_FOUND);
        }

        cardRepository.deleteById(cardId);
    }

    @Override
    @Transactional
    public CardResponse updateCardPosition(Long cardId, Integer newPosition) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND));

        card.setPosition(newPosition);
        Card updatedCard = cardRepository.save(card);
        return cardMapper.toCardResponse(updatedCard);
    }
}