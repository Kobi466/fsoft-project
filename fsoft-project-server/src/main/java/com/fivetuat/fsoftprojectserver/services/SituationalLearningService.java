package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.SituationalLearningResponse;

import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.SituationalLearningRequest;

public interface SituationalLearningService {
    SituationalLearningResponse generateLesson(SituationalLearningRequest request);
}
