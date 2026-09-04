package com.fivetuat.fsoftprojectserver.dtos.https.ai.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VocabCandidate {

    String word;

    String phonetic;

    @JsonProperty("part_of_speech")
    String partOfSpeech;

    String meaning;

    @JsonProperty("definition_en")
    String definitionEn;

    @JsonProperty("example_sentence")
    String exampleSentence;

    @JsonProperty("example_meaning")
    String exampleMeaning;

    @JsonProperty("already_in_deck")
    Boolean alreadyInDeck;

    @JsonProperty("existing_card_id")
    Long existingCardId;
}
