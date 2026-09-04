package com.fivetuat.fsoftprojectserver.dtos.https.ai.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VocabLookupStats {

    String source;

    @JsonProperty("word_chars")
    Integer wordChars;

    @JsonProperty("context_chars")
    Integer contextChars;

    @JsonProperty("llm_calls")
    Integer llmCalls;

    @JsonProperty("cache_size")
    Integer cacheSize;

    @JsonProperty("prompt_tokens")
    Integer promptTokens;

    @JsonProperty("completion_tokens")
    Integer completionTokens;

    @JsonProperty("latency_ms")
    Long latencyMs;
}
