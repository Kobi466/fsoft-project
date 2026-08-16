package com.fivetuat.fsoftprojectserver.dtos.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardAiResponse {
    Long cardId;
    Long deckId;
    String deckTitle;
    String word;
    String phonetic;
    String partOfSpeech;
    String meaning;
    String definitionEn;
    String exampleSentence;
    String exampleMeaning;
    String audioUrl;
    String note;
    Instant updatedAt;
}
