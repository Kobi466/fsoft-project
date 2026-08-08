package com.fivetuat.fsoftprojectserver.dtos.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardPositionRequest {
	Long cardId;
	Integer newPosition;
}
