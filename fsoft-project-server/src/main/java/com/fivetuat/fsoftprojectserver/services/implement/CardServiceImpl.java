package com.fivetuat.fsoftprojectserver.services.implement;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fivetuat.fsoftprojectserver.repositories.DeckRepository;
import com.fivetuat.fsoftprojectserver.utils.ExcelExportHelper;
import com.fivetuat.fsoftprojectserver.utils.PdfExportHelper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CardPositionRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CardUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CardResponse;
import com.fivetuat.fsoftprojectserver.entities.Card;
import com.fivetuat.fsoftprojectserver.entities.Deck;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.enums.FolderName;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.CardMapper;
import com.fivetuat.fsoftprojectserver.repositories.CardRepository;
import com.fivetuat.fsoftprojectserver.services.CardService;
import com.fivetuat.fsoftprojectserver.services.DeckService;
import com.fivetuat.fsoftprojectserver.services.UploadService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CardServiceImpl implements CardService {

    CardRepository cardRepository;
    CardMapper cardMapper;
    DeckService deckService;
    UploadService uploadService;
    DeckRepository deckRepository;
    @Override
    @Transactional
    @CacheEvict(value = "deck_cards", allEntries = true)
    public CardResponse createCard(
            Long deckId,
            CardCreationRequest request,
            MultipartFile imageFile,
            MultipartFile audioFile
    ) {
        log.info("Creating card for deckId {} with word: {}", deckId, request.getWord());
        var deck = this.deckService.getDeckWithEditPermission(deckId);
        Card card = cardMapper.toCard(request);
        card.setDeck(deck);

        checkExistingFile(imageFile, audioFile, card);

        if (card.getPosition() == null) {
            Integer maxPosition = cardRepository.findMaxPositionByDeckId(deckId);
            card.setPosition(maxPosition != null ? maxPosition + 1 : 1);
        }

        Card savedCard = cardRepository.save(card);
        return cardMapper.toCardResponse(savedCard);
    }

    @Override
    @Cacheable(value = "card_detail", key = "#cardId")
    public CardResponse getCardById(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND));
        return cardMapper.toCardResponse(card);
    }

    @Override
    @Cacheable(value = "deck_cards", key = "#deckId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public PageResponse<CardResponse> getCardsByDeckId(Long deckId, Pageable pageable) {
        var deck = this.deckService.getDeck(deckId);
        Page<Card> cardPage = cardRepository.findByDeckIdOrderByPositionAsc(deck.getId(), pageable);
        return PageResponse.<CardResponse>builder()
                .content(cardPage.getContent().stream().map(cardMapper::toCardResponse).toList())
                .pageNo(cardPage.getNumber())
                .pageSize(cardPage.getSize())
                .totalElements(cardPage.getTotalElements())
                .totalPages(cardPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = {"card_detail", "deck_cards"}, allEntries = true)
    public CardResponse updateCard(Long cardId, CardUpdateRequest request, MultipartFile imageFile, MultipartFile audioFile) {
        log.info("Updating card ID: {}", cardId);

        Card card = getCardWithOwnerCheck(cardId);
        cardMapper.updateCard(card, request);

        checkExistingFile(imageFile, audioFile, card);

        Card updatedCard = cardRepository.save(card);
        return cardMapper.toCardResponse(updatedCard);
    }

    private void checkExistingFile(MultipartFile imageFile, MultipartFile audioFile, Card card) {
        if (imageFile != null && !imageFile.isEmpty()){
            if (card.getImageUrl() != null && !card.getImageUrl().isBlank()) {
                uploadService.deleteFile(card.getImageUrl());
            }
            String imageUrl = uploadService.uploadFile(imageFile, FolderName.Card);
            card.setImageUrl(imageUrl);
        }
        if (audioFile != null && !audioFile.isEmpty()){
            if (card.getAudioUrl() != null && !card.getAudioUrl().isBlank()) {
                uploadService.deleteFile(card.getAudioUrl());
            }
            String audioUrl = uploadService.uploadFile(audioFile, FolderName.Card);
            card.setAudioUrl(audioUrl);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"card_detail", "deck_cards"}, allEntries = true)
    public void deleteCard(Long cardId) {
        log.info("Deleting card ID: {}", cardId);

        Card card = getCardWithOwnerCheck(cardId);
        Deck deck = this.deckService.getDeckWithEditPermission(card.getDeck().getId());

        if (card.getImageUrl() != null && !card.getImageUrl().isBlank()) {
            uploadService.deleteFile(card.getImageUrl());
        }
        if (card.getAudioUrl() != null && !card.getAudioUrl().isBlank()) {
            uploadService.deleteFile(card.getAudioUrl());
        }

        cardRepository.delete(card);

        if (deck.getTotalCards() > 0) {
            deck.setTotalCards(deck.getTotalCards() - 1);
            this.deckRepository.save(deck);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "deck_cards", allEntries = true)
    public void updateCardPositions(Long deckId, List<CardPositionRequest> requests) {
        log.info("Updating card position for deck ID: {}", deckId);
        this.deckService.getDeckWithEditPermission(deckId);
        Map<Long, Integer> positionMap = requests.stream()
                .collect(Collectors.toMap(CardPositionRequest::getCardId, CardPositionRequest::getNewPosition));
        List<Card> listCard = this.cardRepository.findAllById(positionMap.keySet());
        for (Card card : listCard) {
            if (positionMap.containsKey(card.getId())) {
                card.setPosition(positionMap.get(card.getId()));
            }
        }
        this.cardRepository.saveAll(listCard);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCardsToExcel(Long deckId) {
        Deck deck = this.deckService.getDeckWithEditPermission(deckId);
        List<Card> cards = cardRepository.findByDeckIdOrderByPositionAsc(deck.getId());
        return ExcelExportHelper.exportCardsToExcel(deck, cards);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCardsToPdf(Long deckId) {
        Deck deck = this.deckService.getDeckWithEditPermission(deckId);
        List<Card> cards = cardRepository.findByDeckIdOrderByPositionAsc(deck.getId());
        return PdfExportHelper.exportCardsToPdf(deck, cards);
    }

    private Card getCardWithOwnerCheck(Long cardId){
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND));
        this.deckService.getDeckWithEditPermission(card.getDeck().getId());
        return card;
    }
}