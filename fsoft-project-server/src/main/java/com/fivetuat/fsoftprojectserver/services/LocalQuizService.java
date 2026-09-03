package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.request.QuizGenerateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.QuizGenerateResponse;

public interface LocalQuizService {
    QuizGenerateResponse generateLocalQuiz(QuizGenerateRequest request);
}
