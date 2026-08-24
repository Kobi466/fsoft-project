package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.request.SrsReviewRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.ReviewCardResponse;

import java.util.List;

public interface ReviewCardService {
	List<ReviewCardResponse> getStudyQueue(Long deckId, Integer limit);

	List<ReviewCardResponse> getFsrsStudyQueue(Long deckId, Integer maxEarlyBufferMinutes, Integer clusterWindowMinutes);

	ReviewCardResponse reviewCard(SrsReviewRequest request);

	void resetDeckProgress(Long deckId);
}
