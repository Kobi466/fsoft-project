package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.MagicSortResult;
import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.MagicSortRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CardSuggestionResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface AiExtractionService {
    List<CardCreationRequest> extractedText(String text, String category);
    List<CardCreationRequest> extractedUrl(String url, String category);
    List<CardCreationRequest> extractedFile(MultipartFile file, String category);
    List<MagicSortResult> magicSort(MagicSortRequest request);
    CardSuggestionResponse getSuggestions(String query);
}
