package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.response.CardSuggestionResponse;

public interface AiSuggestionService {
    CardSuggestionResponse getSuggestions(String query);
}
