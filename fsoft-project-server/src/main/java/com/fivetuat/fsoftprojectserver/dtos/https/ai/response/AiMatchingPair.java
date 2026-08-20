package com.fivetuat.fsoftprojectserver.dtos.https.ai.response;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiMatchingPair {
    @JsonProperty("card_id") Long cardId;
    @JsonProperty("word") String word;
    @JsonProperty("correct_option_index") Integer correctOptionIndex;
}
