package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface AiExtractionService {
    List<CardCreationRequest> extractedText(String text, String category, Long deckId);
    List<CardCreationRequest> extractedUrl(String url, String category, Long deckId);
    List<CardCreationRequest> extractedFile(MultipartFile file, String category, Long deckId);
}
