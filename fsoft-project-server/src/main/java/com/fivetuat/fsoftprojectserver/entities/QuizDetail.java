package com.fivetuat.fsoftprojectserver.entities;

import com.fivetuat.fsoftprojectserver.enums.QuestionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "quiz_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_result_id", nullable = false)
    QuizResult quizResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    Card card;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    QuestionType questionType;

    @Lob
    @Column(name = "user_answer", columnDefinition = "TEXT")
    String userAnswer;

    @Lob
    @Column(name = "correct_answer", columnDefinition = "TEXT")
    String correctAnswer;

    @Column(name = "is_correct", nullable = false)
    boolean isCorrect;

    @Column(name = "score_earned", nullable = false)
    float scoreEarned;
}
