package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.request.QuizSubmitRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.QuizAnalyticsResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.QuizResultResponse;

import java.util.List;

public interface QuizResultService {
    QuizResultResponse submitQuiz(QuizSubmitRequest request);
    List<QuizResultResponse> getMyQuizHistory();
    QuizResultResponse getQuizResultById(Long id);
    QuizAnalyticsResponse getMyQuizAnalytics();
}
