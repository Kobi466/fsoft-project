package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.entities.UserCardSrs;
import com.fivetuat.fsoftprojectserver.enums.SrsStatus;
import com.fivetuat.fsoftprojectserver.services.ReviewAlgorithmService;
import com.fivetuat.fsoftprojectserver.utils.FsrsParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ReviewAlgorithmImpl implements ReviewAlgorithmService {

    /**
     * Thực thi Thuật toán FSRS (Free Spaced Repetition Scheduler) dựa trên mô hình trí nhớ DSR (Difficulty - Stability - Retrievability).
     *
     * @param userCardSrs Đối tượng theo dõi tiến trình SRS của thẻ bài với người dùng
     * @param grade Điểm đánh giá mức độ nhớ của người dùng:
     *              1: Again (Quên), 2: Hard (Khó), 3: Good (Tốt), 4: Easy (Dễ)
     */
    @Override
    public void applyFsrsAlgorithm(UserCardSrs userCardSrs, int grade) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastReviewed = userCardSrs.getLastReviewedAt();

        // Chuẩn hóa grade trong khoảng [1, 4]
        if (grade < 1) grade = 1;
        if (grade > 4) grade = 4;

        Double currentStability = userCardSrs.getStability();
        Double currentDifficulty = userCardSrs.getDifficulty();

        double newStability;
        double newDifficulty;

        if (currentStability == null || currentDifficulty == null) {
            // ====================================================
            // 1. FIRST REVIEW (Lần đầu tiên học thẻ mới)
            // ====================================================
            newStability = getInitialStability(grade);
            newDifficulty = getInitialDifficulty(grade);
            userCardSrs.setRepetitions(grade > 1 ? 1 : 0);
        } else {
            // Tính số ngày trôi qua (t) từ lần ôn tập gần nhất
            double t = 0.0;
            if (lastReviewed != null) {
                long minutes = Duration.between(lastReviewed, now).toMinutes();
                t = Math.max(0.0, minutes / (24.0 * 60.0));
            }

            if (t < 0.01) {
                // ====================================================
                // 2. SHORT-TERM REVIEW (Ôn tập ngắn hạn trong cùng ngày, t = 0)
                // ====================================================
                newDifficulty = calculateNewDifficulty(currentDifficulty, grade);
                newStability = currentStability
                        * Math.exp(FsrsParameters.W17 * (grade - 3 + FsrsParameters.W18))
                        * Math.pow(currentStability, -FsrsParameters.W19);
                if (grade > 1) {
                    userCardSrs.setRepetitions(userCardSrs.getRepetitions() + 1);
                } else {
                    userCardSrs.setRepetitions(0);
                }
            } else {
                // ====================================================
                // 3. SUBSEQUENT REVIEW (Ôn tập sau t > 0 ngày)
                // ====================================================
                // Tính Retrievability (R) tại thời điểm ôn tập
                double r = Math.pow(1.0 + FsrsParameters.FACTOR * (t / currentStability), -FsrsParameters.W20);

                // Cập nhật Độ khó mới (D_new) với cơ chế kéo về mức cơ sở
                newDifficulty = calculateNewDifficulty(currentDifficulty, grade);

                if (grade == 1) {
                    // Trường hợp Quên (Again)
                    double sLapse = FsrsParameters.W11
                            * Math.pow(currentDifficulty, -FsrsParameters.W12)
                            * (Math.pow(currentStability + 1.0, FsrsParameters.W13) - 1.0)
                            * Math.exp((1.0 - r) * FsrsParameters.W14);

                    double sDecay = currentStability / Math.exp(FsrsParameters.W17 * FsrsParameters.W18);
                    newStability = Math.min(sLapse, sDecay);
                    userCardSrs.setRepetitions(0);
                } else {
                    // Trường hợp Nhớ (Hard = 2, Good = 3, Easy = 4)
                    double factor = 1.0;
                    if (grade == 2) {
                        factor = FsrsParameters.W15; // Phạt khi bấm Hard (0.6014)
                    } else if (grade == 4) {
                        factor = FsrsParameters.W16; // Thưởng khi bấm Easy (1.8729)
                    }

                    newStability = currentStability * (1.0 + Math.exp(FsrsParameters.W8)
                            * (11.0 - currentDifficulty)
                            * Math.pow(currentStability, -FsrsParameters.W9)
                            * (Math.exp((1.0 - r) * FsrsParameters.W10) - 1.0)
                            * factor);

                    userCardSrs.setRepetitions(userCardSrs.getRepetitions() + 1);
                }
            }
        }

        // Cập nhật S, D và thời gian ôn gần nhất
        userCardSrs.setStability(newStability);
        userCardSrs.setDifficulty(newDifficulty);
        userCardSrs.setLastReviewedAt(now);

        // Tính khoảng cách ôn tập tiếp theo (Next Interval in days) với R_target = 0.90
        int newIntervalDays = (int) Math.round(newStability);
        userCardSrs.setInterval(Math.max(0, newIntervalDays));

        // Cập nhật trạng thái và thời gian ôn tiếp theo (nextReviewDate)
        SrsStatus newStatus;
        if (grade == 1) {
            newStatus = SrsStatus.LEARNING;
            userCardSrs.setNextReviewDate(now.plusMinutes(10));
        } else {
            newStatus = (newIntervalDays >= 30) ? SrsStatus.REVIEW : SrsStatus.LEARNING;
            if (newIntervalDays < 1) {
                long minutes = Math.max(10, Math.round(newStability * 24.0 * 60.0));
                userCardSrs.setNextReviewDate(now.plusMinutes(minutes));
            } else {
                userCardSrs.setNextReviewDate(now.plusDays(newIntervalDays));
            }
        }
        userCardSrs.setStatus(newStatus);
    }

    private double getInitialStability(int grade) {
        switch (grade) {
            case 1: return FsrsParameters.W0; // 0.2120 ngày (~5h)
            case 2: return FsrsParameters.W1; // 1.2931 ngày
            case 3: return FsrsParameters.W2; // 2.3065 ngày
            case 4: return FsrsParameters.W3; // 8.2956 ngày
            default: return FsrsParameters.W2;
        }
    }

    private double getInitialDifficulty(int grade) {
        // D0 = clamp(w4 - e^(w5 * (grade - 1)) + 1, 1.0, 10.0)
        double d0 = FsrsParameters.W4 - Math.exp(FsrsParameters.W5 * (grade - 1)) + 1.0;
        return FsrsParameters.clampDifficulty(d0);
    }

    private double calculateNewDifficulty(double currentD, int grade) {
        // Delta D = -w6 * (grade - 3)
        double deltaD = -FsrsParameters.W6 * (grade - 3);
        double dPrime = currentD + ((10.0 - currentD) / 9.0) * deltaD;
        // Mean Reversion: w7 * 1.0 + (1 - w7) * dPrime
        double newD = FsrsParameters.W7 * 1.0 + (1.0 - FsrsParameters.W7) * dPrime;
        return FsrsParameters.clampDifficulty(newD);
    }

    @Override
    public void applySm2Algorithm(UserCardSrs userCardSrs, int q) {
        // Giữ lại SM-2 làm phương thức dự phòng
        float currentEf = userCardSrs.getEasinessFactor();
        float newEf = currentEf + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f));
        if (newEf < 1.3f) newEf = 1.3f;
        userCardSrs.setEasinessFactor(newEf);

        int newRepetitions;
        int newInterval;
        SrsStatus newStatus;

        if (q < 2) {
            newRepetitions = 0;
            newInterval = 1;
            newStatus = SrsStatus.LEARNING;
        } else {
            newRepetitions = userCardSrs.getRepetitions() + 1;
            if (newRepetitions == 1) {
                newInterval = 1;
            } else if (newRepetitions == 2) {
                newInterval = 6;
            } else {
                newInterval = Math.round(userCardSrs.getInterval() * newEf);
            }
            newStatus = (newInterval >= 30) ? SrsStatus.REVIEW : SrsStatus.LEARNING;
        }
        userCardSrs.setRepetitions(newRepetitions);
        userCardSrs.setInterval(newInterval);
        userCardSrs.setStatus(newStatus);

        if (q < 2) {
            userCardSrs.setNextReviewDate(LocalDateTime.now().plusMinutes(10));
        } else {
            userCardSrs.setNextReviewDate(LocalDateTime.now().plusDays(newInterval));
        }
    }
}
