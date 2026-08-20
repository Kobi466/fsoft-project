package com.fivetuat.fsoftprojectserver.dtos.https.ai.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiSearchResponse {
    
    @JsonProperty("results")
    List<AiSearchResultItem> results;
    
    @JsonProperty("latency_ms")
    Integer latencyMs;
    
    @JsonProperty("candidate_count")
    Integer candidateCount;
}
