package com.fivetuat.fsoftprojectserver.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardUpdateRequest {

    @NotBlank(message = "WORD_REQUIRED")
    String word;

    String phonetic;

    String partOfSpeech;

    @NotBlank(message = "MEANING_REQUIRED")
    String meaning;

    String definitionEn;

    String exampleSentence;

    String exampleMeaning;

    String imageUrl;

    String audioUrl;

    String note;

    Integer position;
}