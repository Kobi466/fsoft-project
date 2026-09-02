package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.request.AiAutoDeckRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AiAutoDeckResponse;

public interface AiAutoDeckService {
    AiAutoDeckResponse generateAutoDeck(AiAutoDeckRequest request);
}
