package com.fivetuat.fsoftprojectserver.dtos.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CrammingQueueResponse {
    Long deckId;
    String deckTitle;
    int totalCards;
    List<CrammingCardResponse> cards;
}
