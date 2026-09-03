package com.fivetuat.fsoftprojectserver.dtos.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizQuestionResponse {
    Integer index;
    Long cardId;
    String word;
    String meaning;
    String questionText;
    String questionType;
    List<String> choices;
    Integer correctIndex;
    String correctAnswer;
    String explanation;
    String audioUrl;
    String engine;
}
