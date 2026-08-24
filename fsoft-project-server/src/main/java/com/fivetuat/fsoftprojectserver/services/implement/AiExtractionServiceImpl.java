package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.ExtractedCards;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.repositories.CardRepository;
import com.fivetuat.fsoftprojectserver.services.AiExtractionService;
import com.fivetuat.fsoftprojectserver.services.CardService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.content.Media;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AiExtractionServiceImpl implements AiExtractionService {

    ChatClient groqClient;
    ChatClient geminiClient;
    CardService cardService;
    CardRepository cardRepository;

    public AiExtractionServiceImpl(
            @Qualifier("groqClient") ChatClient groqClient,
            @Qualifier("geminiVisionClient") ChatClient geminiClient,
            CardService cardService, 
            CardRepository cardRepository) {
        this.groqClient = groqClient;
        this.geminiClient = geminiClient;
        this.cardService = cardService;
        this.cardRepository = cardRepository;
    }

    public List<CardCreationRequest> extractedText(String text, String category, Long deckId) {
        log.info("Bắt đầu trích xuất từ vựng nhóm '{}', chiều dài text: {}", category, text.length());

        BeanOutputConverter<ExtractedCards> converter = new BeanOutputConverter<>(ExtractedCards.class);
        String format = converter.getFormat();

        PromptTemplate template = new PromptTemplate(
                "Hãy trích xuất các từ vựng quan trọng nhất thuộc nhóm/chủ đề '{category}' từ văn bản bên dưới.\n" +
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
            String response = groqClient.prompt()
                    .user(template.create(variables).getContents())
                    .call()
                    .content();

            ExtractedCards result = converter.convert(response);
            
            if (result == null || result.cards() == null) {
                return List.of();
            }

            java.util.List<CardCreationRequest> savedCards = new java.util.ArrayList<>();

            for (CardCreationRequest card : result.cards()) {
                if (card.getWord() == null || card.getWord().isBlank()) continue;
                
                if (cardRepository.existsByWordAndDeckId(card.getWord().trim(), deckId)) {
                    log.info("Từ vựng '{}' đã có trong bộ thẻ, bỏ qua.", card.getWord());
                    continue;
                }

                if (card.getMeaning() == null) card.setMeaning(card.getWord());
                
                try {
                    cardService.createCard(deckId, card, null, null);
                    savedCards.add(card);
                } catch (Exception e) {
                    log.warn("Lỗi khi lưu thẻ {}: {}", card.getWord(), e.getMessage());
                }
            }

            return savedCards;
        } catch (Exception e) {
            log.error("Lỗi khi gọi AI trích xuất: {}", e.getMessage());
            throw new AppException(ErrorCode.AI_EXTRACTION_ERROR);
        }
    }
    
    @Override
    public List<CardCreationRequest> extractedUrl(String url, String category, Long deckId) {
        log.info("Trích xuất từ URL: {}, nhóm: {}", url, category);
        try {
            Document doc = Jsoup.connect(url).get();
            log.info("Đã tải xong nội dung từ URL, chiều dài text: {}", doc.text().length());
            String text = doc.text();
            log.info("Đã trích xuất {} từ URL, chiều dài text: {}", text.length(), text.length());
            return extractedText(text, category, deckId);
        } catch (Exception e) {
            log.error("Lỗi đọc URL: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.URL_READ_ERROR);
        }
    }

    private List<CardCreationRequest> extractWithGemini(MultipartFile file, String mimeType, String category, Long deckId) throws Exception {
        log.info("Bắt đầu trích xuất đa phương thức (Gemini) cho nhóm '{}', mime: {}", category, mimeType);

        BeanOutputConverter<ExtractedCards> converter = new BeanOutputConverter<>(ExtractedCards.class);
        String format = converter.getFormat();
        
        String promptText = String.format("Hãy trích xuất TỐI ĐA 10 từ vựng quan trọng nhất thuộc nhóm/chủ đề '%s' từ tệp đính kèm.\n" +
                "Yêu cầu:\n" +
                "- Lấy nghĩa tiếng Việt (meaning).\n" +
                "- Lấy 1 câu ví dụ nguyên gốc từ văn bản trong hình/file (exampleSentence).\n" +
                "- Trả về kết quả theo ĐÚNG định dạng JSON sau: %s", category, format);
        byte[] bytes = file.getBytes();
        Resource imageResource = new ByteArrayResource(bytes);
        Media media = new Media(
                MimeTypeUtils.parseMimeType(file.getContentType()), imageResource
        );
        

        String response = geminiClient.prompt()
                .user(promptUserSpec -> promptUserSpec.text(promptText).media(media))
                .call()
                .content();

        ExtractedCards result = converter.convert(response);

        if (result == null || result.cards() == null) {
            return List.of();
        }

        List<CardCreationRequest> savedCards = new java.util.ArrayList<>();

        for (CardCreationRequest card : result.cards()) {
            if (card.getWord() == null || card.getWord().isBlank()) continue;

            if (cardRepository.existsByWordAndDeckId(card.getWord().trim(), deckId)) {
                log.info("Từ vựng '{}' đã có trong bộ thẻ, bỏ qua.", card.getWord());
                continue;
            }

            if (card.getMeaning() == null) card.setMeaning(card.getWord());

            try {
                cardService.createCard(deckId, card, null, null);
                savedCards.add(card);
            } catch (Exception e) {
                log.warn("Lỗi khi lưu thẻ {}: {}", card.getWord(), e.getMessage());
            }
        }

        return savedCards;
    }

    @Override
    public List<CardCreationRequest> extractedFile(MultipartFile file, String category, Long deckId) {
        log.info("Trích xuất từ File: {}, nhóm: {}", file.getOriginalFilename(), category);
        try {
            String mimeType = file.getContentType();
            
            if (mimeType != null && (mimeType.startsWith("image/") || mimeType.equals("application/pdf"))) {
                return extractWithGemini(file, mimeType, category, deckId);
            }
            
            Tika tika = new Tika();
            String text = tika.parseToString(file.getInputStream());
            if (text == null || text.trim().isEmpty()) {
                throw new AppException(ErrorCode.FILE_READ_ERROR);
            }
            return extractedText(text, category, deckId);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi đọc File: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.FILE_READ_ERROR);
        }
    }
}
