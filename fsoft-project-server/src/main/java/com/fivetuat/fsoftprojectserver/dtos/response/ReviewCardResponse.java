package com.fivetuat.fsoftprojectserver.dtos.response;

import com.fivetuat.fsoftprojectserver.enums.SrsStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewCardResponse {
	CardResponse card;
	SrsStatus status;
	int repetitions;
	int interval;
	float easinessFactor;
	Double stability;
	Double difficulty;
	LocalDate nextReviewDate;
}

