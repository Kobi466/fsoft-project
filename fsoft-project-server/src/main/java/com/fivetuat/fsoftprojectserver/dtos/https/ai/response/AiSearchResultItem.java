package com.fivetuat.fsoftprojectserver.dtos.https.ai.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiSearchResultItem {
    
    @JsonProperty("card_id")
    Long cardId;
    
    @JsonProperty("word")
    String word;
    
    @JsonProperty("meaning")
    String meaning;
    
    @JsonProperty("deck_id")
    Long deckId;
    
    @JsonProperty("deck_title")
    String deckTitle;
    
    @JsonProperty("score")
    Double score;
    
    @JsonProperty("match_type")
    String matchType;
}
