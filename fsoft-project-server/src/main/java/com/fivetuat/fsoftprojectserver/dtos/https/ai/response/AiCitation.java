package com.fivetuat.fsoftprojectserver.dtos.https.ai.response;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiCitation {
    @JsonProperty("card_id") Long cardId;
    @JsonProperty("word") String word;
    @JsonProperty("deck_id") Long deckId;
    @JsonProperty("score") Double score;
    @JsonProperty("rank") Integer rank;
    @JsonProperty("used_in_answer") Boolean usedInAnswer;
}
