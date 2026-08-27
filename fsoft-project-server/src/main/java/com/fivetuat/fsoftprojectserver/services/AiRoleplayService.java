package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.request.AiRoleplayRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AiRoleplayResponse;

public interface AiRoleplayService {
    AiRoleplayResponse processRoleplay(AiRoleplayRequest request);
}
