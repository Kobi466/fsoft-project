package com.fivetuat.fsoftprojectserver.dtos.https.ai.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiQuizRequest {
    @JsonProperty("deck_id")
    Long deckId;
    @io.swagger.v3.oas.annotations.media.Schema(hidden = true)
    @JsonProperty("allowed_deck_ids")
    List<Long> allowedDeckIds;
    @JsonProperty("question_count")
    Integer questionCount;
    @JsonProperty("types")
    List<String> types;
    @JsonProperty("card_ids")
    List<Long> cardIds;
    @JsonProperty("use_ai_context")
    Boolean useAiContext;
}
