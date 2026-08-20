package com.fivetuat.fsoftprojectserver.dtos.https.ai.response;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiQuizStats {
    @JsonProperty("deterministic_count") Integer deterministicCount;
    @JsonProperty("llm_count") Integer llmCount;
    @JsonProperty("prompt_tokens") Integer promptTokens;
    @JsonProperty("completion_tokens") Integer completionTokens;
    @JsonProperty("latency_ms") Integer latencyMs;
}
