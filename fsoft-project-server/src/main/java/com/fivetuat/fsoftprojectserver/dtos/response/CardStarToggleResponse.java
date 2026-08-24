package com.fivetuat.fsoftprojectserver.dtos.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardStarToggleResponse {
    Long cardId;
    Boolean isStarred;
}
