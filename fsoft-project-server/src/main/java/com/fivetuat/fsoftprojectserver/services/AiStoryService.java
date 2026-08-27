package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.request.AiStoryRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AiStoryResponse;

public interface AiStoryService {
    AiStoryResponse generateStory(AiStoryRequest request);
}
