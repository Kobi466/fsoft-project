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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.ExtractedCards;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.services.AiExtractionService;
import com.fivetuat.fsoftprojectserver.utils.AiFallbackExecutor;
import com.fivetuat.fsoftprojectserver.utils.AiResponseUtils;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AiExtractionServiceImpl implements AiExtractionService {

    ChatClient groqClient;
    ChatClient geminiClient;
    AiFallbackExecutor aiFallbackExecutor;
    Resource textSystemPromptResource;
    Resource textUserPromptResource;
    Resource multimodalSystemPromptResource;
    Resource multimodalUserPromptResource;

    public AiExtractionServiceImpl(
            @Qualifier("groqClient") ChatClient groqClient,
            @Qualifier("geminiVisionClient") ChatClient geminiClient,
            AiFallbackExecutor aiFallbackExecutor,
            @Value("classpath:/prompts/extraction-system.st") Resource textSystemPromptResource,
            @Value("classpath:/prompts/extraction-user.st") Resource textUserPromptResource,
            @Value("classpath:/prompts/extraction-system.st") Resource multimodalSystemPromptResource,
            @Value("classpath:/prompts/extraction-multimodal-user.st") Resource multimodalUserPromptResource) {
        this.groqClient = groqClient;
        this.geminiClient = geminiClient;
        this.aiFallbackExecutor = aiFallbackExecutor;
        this.textSystemPromptResource = textSystemPromptResource;
        this.textUserPromptResource = textUserPromptResource;
        this.multimodalSystemPromptResource = multimodalSystemPromptResource;
        this.multimodalUserPromptResource = multimodalUserPromptResource;
    }

    @Override
    @Retry(name = "aiRetry", fallbackMethod = "handleExtractedTextRetryFallback")
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
            log.error("Lỗi khi gọi AI (Groq) bóc tách Text: {}", e.getMessage(), e);
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
                MimeTypeUtils.parseMimeType(file.getContentType()),
                imageResource
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
    @Retry(name = "aiRetry", fallbackMethod = "handleExtractedFileRetryFallback")
    public List<CardCreationRequest> extractedFile(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            return List.of();
        }

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

    public List<CardCreationRequest> handleExtractedTextRetryFallback(String text, String category, Throwable t) {
        String safeCategory = (category != null && !category.trim().isEmpty()) ? category : "VOCABULARY";

        ExtractedCards result = aiFallbackExecutor.executeGeminiFallback(
                textSystemPromptResource,
                null,
                textUserPromptResource,
                Map.of("category", safeCategory, "text", text),
                ExtractedCards.class,
                t
        );

        if (result != null && result.cards() != null) {
            List<CardCreationRequest> validCards = new ArrayList<>();
            for (CardCreationRequest card : result.cards()) {
                if (card.getWord() != null && !card.getWord().isBlank()) {
                    if (card.getMeaning() == null) card.setMeaning(card.getWord());
                    validCards.add(card);
                }
            }
            return validCards;
        }
        return List.of();
    }

    public List<CardCreationRequest> handleExtractedFileRetryFallback(MultipartFile file, String category, Throwable t) {
        log.error("Retry thất bại khi trích xuất file AI. Lỗi: {}", t != null ? t.getMessage() : "N/A");
        return List.of();
    }
}
