package com.fivetuat.fsoftprojectserver.dtos.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * init algorithm
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SrsReviewRequest {
	@NotNull(message = "Card ID is required")
	Long cardId;

	@Min(value = 1, message = "Rating must be between 1 and 4")
	@Max(value = 4, message = "Rating must be between 1 and 4")
	int rating;
}
