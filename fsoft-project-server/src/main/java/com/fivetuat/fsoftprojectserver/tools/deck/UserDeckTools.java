package com.fivetuat.fsoftprojectserver.tools.deck;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CardResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import com.fivetuat.fsoftprojectserver.services.CardService;
import com.fivetuat.fsoftprojectserver.services.DeckService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserDeckTools {

    DeckService deckService;
    CardService cardService;

    public record CardItemInput(
            @ToolParam(description = "Từ vựng hoặc thuật ngữ tiếng Anh (VD: 'Apple', 'Encapsulation')")
            String word,
            @ToolParam(description = "Nghĩa dịch tiếng Việt (VD: 'Quả táo', 'Tính đóng gói')")
            String meaning,
            @ToolParam(description = "Phiên âm IPA chuẩn (VD: '/ˈæp.əl/')")
            String phonetic,
            @ToolParam(description = "Câu ví dụ minh họa bằng tiếng Anh (VD: 'She ate a red apple.')")
            String exampleSentence
    ) {}


    @Tool(description = "Lấy danh sách tất cả các Bộ bài Flashcard (Deck) của bản thân học viên hiện tại bao gồm ID, tiêu đề, và số lượng thẻ.")
    public List<DeckResponse> getMyDecks() {
        log.info("Tool called: getMyDecks");
        PageResponse<DeckResponse> page = deckService.getMyDecks(1, 50);
        return page.getContent();
    }


    @Tool(description = "Tạo một Bộ bài Flashcard mới kèm theo danh sách đầy đủ các thẻ từ vựng (Cards) và lưu trực tiếp vào Database của học viên.")
    public DeckResponse createDeckWithCards(
            @ToolParam(description = "Tiêu đề bộ bài chuẩn và hấp dẫn (VD: '10 Từ vựng phỏng vấn IT ReactJS')") String title,
            @ToolParam(description = "Mô tả chi tiết mục tiêu học tập của bộ bài") String description,
            @ToolParam(description = "Mã ngôn ngữ nguồn (mặc định 'en')") String sourceLanguage,
            @ToolParam(description = "Mã ngôn ngữ đích (mặc định 'vi')") String targetLanguage,
            @ToolParam(description = "Danh sách các thẻ từ vựng hoàn chỉnh (gồm word, meaning, phonetic, exampleSentence)") List<CardItemInput> cards
    ) {
        log.info("Tool called: createDeckWithCards title='{}', cardCount={}", title, cards != null ? cards.size() : 0);
        
        DeckCreateRequest deckCreateRequest = DeckCreateRequest.builder()
                .title(title)
                .description(description)
                .sourceLanguage(sourceLanguage != null && !sourceLanguage.isBlank() ? sourceLanguage : "en")
                .targetLanguage(targetLanguage != null && !targetLanguage.isBlank() ? targetLanguage : "vi")
                .visibility(DeckVisibility.PRIVATE)
                .build();
        DeckResponse createdDeck = deckService.createDeck(deckCreateRequest, null);

        if (cards != null) {
            for (CardItemInput cardItem : cards) {
                CardCreationRequest cardReq = CardCreationRequest.builder()
                        .word(cardItem.word())
                        .meaning(cardItem.meaning())
                        .phonetic(cardItem.phonetic())
                        .exampleSentence(cardItem.exampleSentence())
                        .build();
                cardService.createCard(createdDeck.getId(), cardReq, null, null);
            }
        }
        return createdDeck;
    }


    @Tool(description = "Tạo một Bộ bài Flashcard trống mới cho học viên với tiêu đề, mô tả và ngôn ngữ nguồn/đích.")
    public DeckResponse createDeck(
            @ToolParam(description = "Tiêu đề bộ bài (VD: 'Từ vựng IELTS Reading')") String title,
            @ToolParam(description = "Mô tả ngắn gọn về bộ bài") String description,
            @ToolParam(description = "Mã ngôn ngữ nguồn (VD: 'en', 'ja', 'vi')") String sourceLanguage,
            @ToolParam(description = "Mã ngôn ngữ đích (VD: 'vi', 'en')") String targetLanguage
    ) {
        log.info("Tool called: createDeck with title='{}'", title);
        DeckCreateRequest request = DeckCreateRequest.builder()
                .title(title)
                .description(description)
                .sourceLanguage(sourceLanguage != null && !sourceLanguage.isBlank() ? sourceLanguage : "en")
                .targetLanguage(targetLanguage != null && !targetLanguage.isBlank() ? targetLanguage : "vi")
                .visibility(DeckVisibility.PRIVATE)
                .build();
        return deckService.createDeck(request, null);
    }

    
    @Tool(description = "Thêm một thẻ từ vựng mới (Word, Meaning, Example, Phonetic) vào Bộ bài có sẵn dựa trên deckId.")
    public CardResponse addCardToDeck(
            @ToolParam(description = "ID của bộ bài cần thêm thẻ (lấy từ getMyDecks hoặc sau khi tạo deck)") Long deckId,
            @ToolParam(description = "Từ vựng tiếng Anh/ngôn ngữ gốc (VD: 'Resilient')") String word,
            @ToolParam(description = "Nghĩa tiếng Việt của từ (VD: 'Kiên cường, phục hồi nhanh')") String meaning,
            @ToolParam(description = "Phiên âm IPA chuẩn (VD: '/rɪˈzɪl.jənt/')") String phonetic,
            @ToolParam(description = "Câu ví dụ sử dụng từ (VD: 'He is resilient under pressure.')") String exampleSentence
    ) {
        log.info("Tool called: addCardToDeck deckId={} word='{}'", deckId, word);
        CardCreationRequest request = CardCreationRequest.builder()
                .word(word)
                .meaning(meaning)
                .phonetic(phonetic)
                .exampleSentence(exampleSentence)
                .build();
        return cardService.createCard(deckId, request, null, null);
    }
}
