package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.request.SrsReviewRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.SrsCardResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.UserActivityResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.UserStreakResponse;

import java.time.LocalDate;
import java.util.List;

public interface SrsService {
	List<SrsCardResponse> getStudyQueue(Long deckId, Integer limit);

	SrsCardResponse reviewCard(SrsReviewRequest request);

	void resetDeckProgress(Long deckId);
}
