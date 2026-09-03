package com.fivetuat.fsoftprojectserver.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizGenerateRequest {
    Long deckId;

    @Schema(hidden = true)
    List<Long> allowedDeckIds;

    Integer questionCount;

    List<String> types;

    List<Long> cardIds;

    Boolean useAiContext;

    Boolean isSrsOnly;
}
