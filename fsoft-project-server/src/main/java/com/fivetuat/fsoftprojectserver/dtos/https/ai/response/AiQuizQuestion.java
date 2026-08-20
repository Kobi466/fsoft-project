package com.fivetuat.fsoftprojectserver.dtos.https.ai.response;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiQuizQuestion {
    @JsonProperty("index") Integer index;
    @JsonProperty("type") String type;
    @JsonProperty("card_id") Long cardId;
    @JsonProperty("prompt") String prompt;
    @JsonProperty("options") List<String> options;
    @JsonProperty("correct_index") Integer correctIndex;
    @JsonProperty("explanation") String explanation;
    @JsonProperty("generated_by") String generatedBy;
    @JsonProperty("audio_url") String audioUrl;
    @JsonProperty("matching") List<AiMatchingPair> matching;
}
