package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.request.QuizSubmitRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.QuizAnalyticsResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.QuizResultResponse;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.entities.QuizResult;
import com.fivetuat.fsoftprojectserver.enums.ActivityType;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.QuizResultMapper;
import com.fivetuat.fsoftprojectserver.repositories.QuizResultRepository;
import com.fivetuat.fsoftprojectserver.services.AuthService;
import com.fivetuat.fsoftprojectserver.services.ProgressService;
import com.fivetuat.fsoftprojectserver.services.QuizResultService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuizResultServiceImpl implements QuizResultService {

    AuthService authService;
    ProgressService progressService;
    QuizResultRepository quizResultRepository;
    QuizResultMapper quizResultMapper;

    @Override
    @Transactional
    public QuizResultResponse submitQuiz(QuizSubmitRequest request) {
        Profile profile = authService.getCurrentProfile();
        log.info("Xử lý nộp bài thi Quiz cho profile ID: {}", profile.getId());

        float accuracyRate = request.getTotalQuestions() > 0 
                ? (float) request.getCorrectAnswers() / request.getTotalQuestions() 
                : 0.0f;

        float baseScore = request.getScore() > 0 ? request.getScore() : request.getCorrectAnswers() * 10;

        QuizResult quizResult = QuizResult.builder()
                .profile(profile)
                .scope(request.getScope())
                .totalQuestions(request.getTotalQuestions())
                .correctAnswers(request.getCorrectAnswers())
                .score(baseScore)
                .accuracyRate(accuracyRate)
                .timeSpentSeconds(request.getTimeSpentSeconds())
                .build();

        QuizResult savedResult = quizResultRepository.save(quizResult);

        // Ghi nhận Streak & Nhật ký hoạt động của User (Dạng Quiz)
        progressService.updateUserActivityAndStreak(profile, LocalDate.now(), ActivityType.QUIZ_COMPLETED);

        // Tính điểm thưởng XP (Bonus +50 XP nếu làm đúng 100%)
        boolean isPerfectScore = accuracyRate >= 1.0f;
        int xpEarned = (int) baseScore + (isPerfectScore ? 50 : 0);

        log.info("Đã lưu thành công bài Quiz ID: {}, điểm: {}, XP: {}, độ chính xác: {}%", 
                savedResult.getId(), savedResult.getScore(), xpEarned, accuracyRate * 100);

        QuizResultResponse response = quizResultMapper.toQuizResultResponse(savedResult);
        response.setXpEarned(xpEarned);
        response.setPerfectScore(isPerfectScore);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizResultResponse> getMyQuizHistory() {
        Profile profile = authService.getCurrentProfile();
        List<QuizResult> results = quizResultRepository.findByProfileIdOrderByCreatedAtDesc(profile.getId());
        return quizResultMapper.toQuizResultResponseList(results);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizResultResponse getQuizResultById(Long id) {
        Profile profile = authService.getCurrentProfile();
        QuizResult result = quizResultRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_RESULT_NOT_FOUND));

        if (!result.getProfile().getId().equals(profile.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        QuizResultResponse response = quizResultMapper.toQuizResultResponse(result);
        response.setXpEarned((int) result.getScore() + (result.getAccuracyRate() >= 1.0f ? 50 : 0));
        response.setPerfectScore(result.getAccuracyRate() >= 1.0f);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public QuizAnalyticsResponse getMyQuizAnalytics() {
        Profile profile = authService.getCurrentProfile();
        List<QuizResult> results = quizResultRepository.findByProfileIdOrderByCreatedAtDesc(profile.getId());

        if (results.isEmpty()) {
            return QuizAnalyticsResponse.builder()
                    .totalQuizzesTaken(0)
                    .averageAccuracyRate(0.0f)
                    .totalScoreEarned(0.0f)
                    .totalTimeSpentSeconds(0)
                    .bestAccuracy(0.0f)
                    .recentResults(Collections.emptyList())
                    .build();
        }

        int totalQuizzesTaken = results.size();
        float averageAccuracy = (float) results.stream().mapToDouble(QuizResult::getAccuracyRate).average().orElse(0.0);
        float totalScore = (float) results.stream().mapToDouble(QuizResult::getScore).sum();
        int totalTime = results.stream().mapToInt(QuizResult::getTimeSpentSeconds).sum();
        float bestAccuracy = (float) results.stream().mapToDouble(QuizResult::getAccuracyRate).max().orElse(0.0);

        List<QuizResultResponse> recentResults = results.stream()
                .limit(7)
                .map(r -> {
                    QuizResultResponse dto = quizResultMapper.toQuizResultResponse(r);
                    dto.setXpEarned((int) r.getScore() + (r.getAccuracyRate() >= 1.0f ? 50 : 0));
                    dto.setPerfectScore(r.getAccuracyRate() >= 1.0f);
                    return dto;
                })
                .collect(Collectors.toList());

        return QuizAnalyticsResponse.builder()
                .totalQuizzesTaken(totalQuizzesTaken)
                .averageAccuracyRate(averageAccuracy)
                .totalScoreEarned(totalScore)
                .totalTimeSpentSeconds(totalTime)
                .bestAccuracy(bestAccuracy)
                .recentResults(recentResults)
                .build();
    }
}
