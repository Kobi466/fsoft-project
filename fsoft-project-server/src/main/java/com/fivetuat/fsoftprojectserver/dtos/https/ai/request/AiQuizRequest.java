package com.fivetuat.fsoftprojectserver.dtos.https.ai.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiQuizRequest {
    @JsonProperty("deck_id")
    Long deckId;
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
