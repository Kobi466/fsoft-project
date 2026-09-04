package com.fivetuat.fsoftprojectserver.dtos.https.ai.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VocabLookupResponse {

    String source;

    Boolean found;

    String suggestion;

    VocabCandidate card;

    VocabLookupStats stats;
}
