package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.ExtractedCards;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.services.AiExtractionService;
import com.fivetuat.fsoftprojectserver.services.CardService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AiExtractionServiceImpl implements AiExtractionService {

    ChatClient chatClient;
    CardService cardService;

    public AiExtractionServiceImpl(ChatClient.Builder chatClientBuilder, CardService cardService) {
        this.chatClient = chatClientBuilder
                .defaultSystem("Bạn là một chuyên gia ngôn ngữ học. Nhiệm vụ của bạn là đọc hiểu văn bản, " +
                               "trích xuất từ vựng khó theo đúng chủ đề được yêu cầu và trả về kết quả chuẩn định dạng JSON. " +
                               "Tuyệt đối KHÔNG trả lời thêm bất kỳ câu văn tự nhiên nào ngoài chuỗi JSON.")
                .build();
        this.cardService = cardService;
    }

    public List<CardCreationRequest> extractedText(String text, String category, Long deckId) {
        log.info("Bắt đầu trích xuất từ vựng nhóm '{}', chiều dài text: {}", category, text.length());

        BeanOutputConverter<ExtractedCards> converter = new BeanOutputConverter<>(ExtractedCards.class);
        String format = converter.getFormat();

        PromptTemplate template = new PromptTemplate(
                "Hãy trích xuất danh sách các từ vựng quan trọng thuộc nhóm/chủ đề '{category}' từ văn bản bên dưới.\n" +
                "Yêu cầu:\n" +
                "- Lấy nghĩa tiếng Việt (meaning).\n" +
                "- Lấy 1 câu ví dụ nguyên gốc từ văn bản (exampleSentence).\n" +
                "- Trả về kết quả theo ĐÚNG định dạng JSON sau: {format}\n\n" +
                "Văn bản gốc:\n{text}"
        );

        Map<String, Object> variables = Map.of(
                "category", category,
                "format", format,
                "text", text
        );

        try {
            String response = chatClient.prompt()
                    .user(template.create(variables).getContents())
                    .call()
                    .content();

            ExtractedCards result = converter.convert(response);
            
            if (result == null || result.cards() == null) {
                return List.of();
            }

            for (CardCreationRequest card : result.cards()) {
                if (card.getWord() == null || card.getWord().isBlank()) continue;
                if (card.getMeaning() == null) card.setMeaning(card.getWord());
                
                try {
                    cardService.createCard(deckId, card, null, null);
                } catch (Exception e) {
                    log.warn("Lỗi khi lưu thẻ {}: {}", card.getWord(), e.getMessage());
                }
            }

            return result.cards();
        } catch (Exception e) {
            log.error("Lỗi khi gọi AI trích xuất: {}", e.getMessage());
            throw new AppException(ErrorCode.AI_EXTRACTION_ERROR);
        }
    }
    
    @Override
    public List<CardCreationRequest> extractedUrl(String url, String category, Long deckId) {
        log.info("Trích xuất từ URL: {}, nhóm: {}", url, category);
        try {
            org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
            String text = doc.text(); 
            return extractedText(text, category, deckId);
        } catch (Exception e) {
            log.error("Lỗi đọc URL: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.URL_READ_ERROR);
        }
    }

    @Override
    public List<CardCreationRequest> extractedFile(MultipartFile file, String category, Long deckId) {
        log.info("Trích xuất từ File: {}, nhóm: {}", file.getOriginalFilename(), category);
        try {
            Tika tika = new Tika();
            String text = tika.parseToString(file.getInputStream());
            return extractedText(text, category, deckId);
        } catch (Exception e) {
            log.error("Lỗi đọc File: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.FILE_READ_ERROR);
        }
    }
}
