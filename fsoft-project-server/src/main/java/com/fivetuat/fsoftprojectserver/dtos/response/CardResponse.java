package com.fivetuat.fsoftprojectserver.dtos.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardResponse {

    Long id;
    Long deckId;
    String word;
    String phonetic;
    String partOfSpeech;
    String meaning;
    String definitionEn;
    String exampleSentence;
    String exampleMeaning;
    String imageUrl;
    String audioUrl;
    String note;
    Integer position;
    Instant createdAt;
    Instant updatedAt;
}