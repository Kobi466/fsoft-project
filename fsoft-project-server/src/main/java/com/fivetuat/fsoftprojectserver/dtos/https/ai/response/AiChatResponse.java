package com.fivetuat.fsoftprojectserver.dtos.https.ai.response;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiChatResponse {
    @JsonProperty("answer") String answer;
    @JsonProperty("intent") String intent;
    @JsonProperty("answer_source") String answerSource;
    @JsonProperty("rewritten_query") String rewrittenQuery;
    @JsonProperty("citations") List<AiCitation> citations;
    @JsonProperty("usage") AiUsage usage;
}
