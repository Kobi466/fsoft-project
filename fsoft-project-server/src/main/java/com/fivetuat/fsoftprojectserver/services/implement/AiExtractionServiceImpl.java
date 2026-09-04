package com.fivetuat.fsoftprojectserver.services.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.content.Media;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.MagicSortCards;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.MagicSortResult;
import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.ExtractedCards;
import com.fivetuat.fsoftprojectserver.dtos.request.MagicSortRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CardSuggestionResponse;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.utils.AiResponseUtils;
import com.fivetuat.fsoftprojectserver.services.AiExtractionService;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AiExtractionServiceImpl implements AiExtractionService {

    ChatClient groqClient;
    ChatClient geminiClient;
    Resource textSystemPromptResource;
    Resource textUserPromptResource;
    Resource multimodalSystemPromptResource;
    Resource multimodalUserPromptResource;
    Resource suggestionSystemPromptResource;
    Resource suggestionUserPromptResource;

    public AiExtractionServiceImpl(
            @Qualifier("groqClient") ChatClient groqClient,
            @Qualifier("geminiVisionClient") ChatClient geminiClient,
            @Value("classpath:/prompts/extraction-system.st") Resource textSystemPromptResource,
            @Value("classpath:/prompts/extraction-user.st") Resource textUserPromptResource,
            @Value("classpath:/prompts/extraction-system.st") Resource multimodalSystemPromptResource,
            @Value("classpath:/prompts/extraction-multimodal-user.st") Resource multimodalUserPromptResource,
            @Value("classpath:/prompts/card-suggestion-system.st") Resource suggestionSystemPromptResource,
            @Value("classpath:/prompts/card-suggestion-user.st") Resource suggestionUserPromptResource) {
        this.groqClient = groqClient;
        this.geminiClient = geminiClient;
        this.textSystemPromptResource = textSystemPromptResource;
        this.textUserPromptResource = textUserPromptResource;
        this.multimodalSystemPromptResource = multimodalSystemPromptResource;
        this.multimodalUserPromptResource = multimodalUserPromptResource;
        this.suggestionSystemPromptResource = suggestionSystemPromptResource;
        this.suggestionUserPromptResource = suggestionUserPromptResource;
    }

    @Override
    public List<CardCreationRequest> extractedText(String text, String category) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("Nội dung text rỗng, bỏ qua trích xuất từ vựng.");
            return List.of();
        }

        String safeCategory = (category != null && !category.trim().isEmpty()) ? category : "VOCABULARY";
        log.info("Bắt đầu trích xuất từ vựng nhóm '{}', chiều dài text: {}", safeCategory, text.length());

        BeanOutputConverter<ExtractedCards> converter = new BeanOutputConverter<>(ExtractedCards.class);
        String format = converter.getFormat();

        SystemPromptTemplate systemTemplate = new SystemPromptTemplate(textSystemPromptResource);
        String systemMessage = systemTemplate.render(Map.of("format", format));

        PromptTemplate userTemplate = new PromptTemplate(textUserPromptResource);
        String userMessage = userTemplate.render(Map.of(
                "category", safeCategory,
                "text", text
        ));

        try {
            String rawResponse = groqClient.prompt()
                    .system(systemMessage)
                    .user(userMessage)
                    .call()
                    .content();

            String cleanedResponse = AiResponseUtils.sanitizeJsonResponse(rawResponse);
            ExtractedCards result = converter.convert(cleanedResponse);

            if (result == null || result.cards() == null) {
                return List.of();
            }

            List<CardCreationRequest> validCards = new ArrayList<>();
            for (CardCreationRequest card : result.cards()) {
                if (card.getWord() == null || card.getWord().isBlank()) {
                    continue;
                }
                if (card.getMeaning() == null) {
                    card.setMeaning(card.getWord());
                }
                validCards.add(card);
            }
            return validCards;

        } catch (Exception e) {
            log.error("Lỗi khi gọi AI trích xuất: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AI_EXTRACTION_ERROR);
        }
    }

    @Override
    public List<CardCreationRequest> extractedUrl(String url, String category) {
        if (url == null || url.trim().isEmpty()) {
            log.warn("URL rỗng, không thể trích xuất.");
            throw new AppException(ErrorCode.URL_READ_ERROR);
        }
        log.info("Trích xuất từ URL: {}, nhóm: {}", url, category);
        try {
            Document doc = Jsoup.connect(url.trim())
                    .timeout(10000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .get();
            log.info("Đã tải xong nội dung từ URL, chiều dài text: {}", doc.text().length());
            String text = doc.text();
            return extractedText(text, category);
        } catch (Exception e) {
            log.error("Lỗi đọc URL: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.URL_READ_ERROR);
        }
    }

    private List<CardCreationRequest> extractWithGemini(MultipartFile file, String mimeType, String category) throws Exception {
        log.info("Bắt đầu trích xuất đa phương thức (Gemini) cho nhóm '{}', mime: {}", category, mimeType);

        BeanOutputConverter<ExtractedCards> converter = new BeanOutputConverter<>(ExtractedCards.class);
        String format = converter.getFormat();

        SystemPromptTemplate systemTemplate = new SystemPromptTemplate(multimodalSystemPromptResource);
        String systemMessage = systemTemplate.render(Map.of("format", format));

        PromptTemplate userTemplate = new PromptTemplate(multimodalUserPromptResource);
        String userMessage = userTemplate.render(Map.of("category", category));

        byte[] bytes = file.getBytes();
        Resource imageResource = new ByteArrayResource(bytes);
        Media media = new Media(
                MimeTypeUtils.parseMimeType(file.getContentType()), imageResource
        );

        String rawResponse = geminiClient.prompt()
                .system(systemMessage)
                .user(promptUserSpec -> promptUserSpec.text(userMessage).media(media))
                .call()
                .content();

        String cleanedResponse = AiResponseUtils.sanitizeJsonResponse(rawResponse);
        ExtractedCards result = converter.convert(cleanedResponse);

        if (result == null || result.cards() == null) {
            return List.of();
        }

        List<CardCreationRequest> validCards = new ArrayList<>();
        for (CardCreationRequest card : result.cards()) {
            if (card.getWord() == null || card.getWord().isBlank()) {
                continue;
            }
            if (card.getMeaning() == null) {
                card.setMeaning(card.getWord());
            }
            validCards.add(card);
        }
        return validCards;
    }

    @Override
    public List<CardCreationRequest> extractedFile(MultipartFile file, String category) {
        log.info("Trích xuất từ File: {}, nhóm: {}", file.getOriginalFilename(), category);
        try {
            String mimeType = file.getContentType();

            if (mimeType != null && (mimeType.startsWith("image/") || mimeType.equals("application/pdf"))) {
                return extractWithGemini(file, mimeType, category);
            }

            Tika tika = new Tika();
            String text = tika.parseToString(file.getInputStream());
            if (text == null || text.trim().isEmpty()) {
                throw new AppException(ErrorCode.FILE_READ_ERROR);
            }
            return extractedText(text, category);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi đọc File: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.FILE_READ_ERROR);
        }
    }

    @Override
    public List<MagicSortResult> magicSort(MagicSortRequest request) {
        log.info("Bắt đầu Magic Sort cho {} từ vựng", request.words().size());

        BeanOutputConverter<MagicSortCards> converter = new BeanOutputConverter<>(MagicSortCards.class);
        String format = converter.getFormat();

        StringBuilder decksInfoBuilder = new StringBuilder();
        if (request.decks() != null) {
            request.decks().forEach((id, name)
                    -> decksInfoBuilder.append("- ID: ").append(id).append(", Tên: ").append(name).append("\n")
            );
        }
        String decksInfo = decksInfoBuilder.toString();

        String wordsStr = String.join(", ", request.words());

        SystemPromptTemplate systemTemplate = new SystemPromptTemplate(new ClassPathResource("/prompts/magic-sort-system.st"));
        String systemMessage = systemTemplate.render(Map.of("format", format));

        PromptTemplate userTemplate = new PromptTemplate(new ClassPathResource("/prompts/magic-sort-user.st"));
        String userMessage = userTemplate.render(Map.of(
                "words", wordsStr,
                "decksInfo", decksInfo
        ));

        try {
            String rawResponse = groqClient.prompt()
                    .system(systemMessage)
                    .user(userMessage)
                    .call()
                    .content();

            String cleanedResponse = AiResponseUtils.sanitizeJsonResponse(rawResponse);
            MagicSortCards result = converter.convert(cleanedResponse);

            if (result == null || result.results() == null) {
                return List.of();
            }

            return result.results();

        } catch (Exception e) {
            log.error("Lỗi khi gọi AI Magic Sort: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AI_EXTRACTION_ERROR);
        }
    }

    @Cacheable(
            value = "ai_suggestions",
            key = "#query.toLowerCase().trim()",
            unless = "#query == null || #query.trim().length() < 3 || #result == null || #result.termSuggestions == null || #result.termSuggestions.isEmpty()"
    )
    @Override
    public CardSuggestionResponse getSuggestions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return CardSuggestionResponse.builder()
                    .query("")
                    .termSuggestions(List.of())
                    .definitionSuggestions(List.of())
                    .build();
        }

        log.info("Bắt đầu AI Autocomplete Suggestion cho từ vựng: '{}'", query);

        BeanOutputConverter<CardSuggestionResponse> converter = new BeanOutputConverter<>(CardSuggestionResponse.class);
        String format = converter.getFormat();

        try {
            SystemPromptTemplate systemTemplate = new SystemPromptTemplate(suggestionSystemPromptResource);
            String systemMessage = systemTemplate.render(Map.of("format", format));

            PromptTemplate userTemplate = new PromptTemplate(suggestionUserPromptResource);
            String userMessage = userTemplate.render(Map.of("query", query.trim()));

            String rawResponse = groqClient.prompt()
                    .system(systemMessage)
                    .user(userMessage)
                    .call()
                    .content();

            String cleanedResponse = AiResponseUtils.sanitizeJsonResponse(rawResponse);
            CardSuggestionResponse result = converter.convert(cleanedResponse);

            if (result == null) {
                return CardSuggestionResponse.builder()
                        .query(query)
                        .termSuggestions(List.of(query))
                        .definitionSuggestions(List.of())
                        .build();
            }

            return result;

        } catch (Exception e) {
            log.error("Lỗi khi gọi AI Autocomplete Suggestion: {}", e.getMessage(), e);
            return CardSuggestionResponse.builder()
                    .query(query)
                    .termSuggestions(List.of(query))
                    .definitionSuggestions(List.of())
                    .build();
        }
    }

}
