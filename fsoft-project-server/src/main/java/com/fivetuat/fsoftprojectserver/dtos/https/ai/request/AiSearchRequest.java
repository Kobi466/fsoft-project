package com.fivetuat.fsoftprojectserver.dtos.https.ai.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiSearchRequest {
    
    @JsonProperty("query")
    String query;
    
    @JsonProperty("allowed_deck_ids")
    List<Long> allowedDeckIds;
    
    @JsonProperty("top_k")
    Integer topK;
    
    @JsonProperty("scope_deck_id")
    Long scopeDeckId;
}
