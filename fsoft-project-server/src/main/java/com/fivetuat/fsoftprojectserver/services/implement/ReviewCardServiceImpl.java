package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.request.SrsReviewRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.ReviewCardResponse;
import com.fivetuat.fsoftprojectserver.entities.Card;
import com.fivetuat.fsoftprojectserver.entities.UserCardSrs;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.enums.SrsStatus;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.CardMapper;
import com.fivetuat.fsoftprojectserver.mappers.UserCardSrsMapper;
import com.fivetuat.fsoftprojectserver.repositories.CardRepository;
import com.fivetuat.fsoftprojectserver.repositories.UserCardSrsRepository;
import com.fivetuat.fsoftprojectserver.services.AuthService;
import com.fivetuat.fsoftprojectserver.services.ProgressService;
import com.fivetuat.fsoftprojectserver.services.ReviewAlgorithmService;
import com.fivetuat.fsoftprojectserver.services.ReviewCardService;
import com.fivetuat.fsoftprojectserver.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ReviewCardServiceImpl implements ReviewCardService {
	UserCardSrsRepository userCardSrsRepository;
	CardRepository cardRepository;
	UserCardSrsMapper userCardSrsMapper;
	AuthService authService;
	CardMapper cardMapper;
	ProgressService progressService;
	ReviewAlgorithmService reviewAlgorithmService;
	@Override
	public List<ReviewCardResponse> getStudyQueue(Long deckId, Integer limit) {
		UUID userId = SecurityUtils.getCurrentUserLogin().orElseThrow(
				() -> new AppException(ErrorCode.AUTHENTICATION_FAILED)
		);
		int maxLimit = (limit != null & limit > 0) ? limit : 20;
		LocalDateTime now = LocalDateTime.now();
		List<UserCardSrs> dueCards;
		if (deckId != null) {
			dueCards = userCardSrsRepository.findByProfileIdAndCardDeckIdAndNextReviewDateLessThanEqualOrderByNextReviewDateAsc(userId, deckId, now);
		} else {
			dueCards = userCardSrsRepository.findByProfileIdAndNextReviewDateLessThanEqualOrderByNextReviewDateAsc(userId, now);
		}
		
		List<ReviewCardResponse> responses = new ArrayList<>();
		for (UserCardSrs userCardSrs : dueCards){
			if (responses.size() >= maxLimit) break;
			responses.add(userCardSrsMapper.toResponse(userCardSrs));
		}
//		if (responses.size() < maxLimit && deckId != null){
//			Set<Long> learnedCardIds = userCardSrsRepository.findLearnedCardIdsByProfileAndDeckId(userId, deckId);
//
//			List<Card> allCardsInDeck = cardRepository.findByDeckIdOrderByPositionAsc(deckId);
//
//			for (Card card : allCardsInDeck){
//				if (responses.size() >= maxLimit) break;
//				if (!learnedCardIds.contains(card.getId())){
//					responses.add(
//							ReviewCardResponse.builder()
//									.card(cardMapper.toCardResponse(card))
//									.status(SrsStatus.NEW)
//									.repetitions(0)
//									.interval(0)
//									.easinessFactor(2.5f)
//									.build()
//					);
//				}
//			}
//		}
		int remainingSlots = maxLimit - responses.size();
		if (remainingSlots > 0 && deckId != null) {
			List<Card> newCards = this.userCardSrsRepository.findUnlearnedCardsByDeckAndProfile(userId, deckId, PageRequest.of(0, remainingSlots));

			for (Card card : newCards) {
				responses.add(
						ReviewCardResponse.builder()
								.card(cardMapper.toCardResponse(card))
								.status(SrsStatus.NEW)
								.repetitions(0)
								.interval(0)
								.easinessFactor(2.5f)
								.stability(2.3065)
								.difficulty(2.1193)
								.build()
				);
			}
		}
		return responses;
	}

	/**
	 * Thực hiện thuật toán FSRS (Free Spaced Repetition Scheduler - 21 parameters DSR model)
	 * để tính toán độ bền (Stability), độ khó (Difficulty) và lịch ôn tập tiếp theo cho flashcard.
	 */
	@Transactional
	@Override
	public ReviewCardResponse reviewCard(SrsReviewRequest request) {
		var profile = this.authService.getCurrentProfile();
		var card = this.cardRepository.findById(request.getCardId()).orElseThrow(
				() -> new AppException(ErrorCode.CARD_NOT_FOUND)
		);
		var userCardSrs = this.userCardSrsRepository.findByProfileIdAndCardId(profile.getId(), card.getId())
				.orElseGet(() ->
							UserCardSrs.builder()
									.profile(profile)
									.card(card)
									.status(SrsStatus.NEW)
									.easinessFactor(2.5f)
									.repetitions(0)
									.interval(0)
									.nextReviewDate(LocalDateTime.now())
									.build()

				);

		reviewAlgorithmService.applyFsrsAlgorithm(userCardSrs, request.getRating());
		//ghi nhan activity va steak
		this.progressService.updateUserActivityAndStreak(profile, LocalDate.now());

		return this.userCardSrsMapper.toResponse(
				this.userCardSrsRepository.save(userCardSrs)
		);
	}

	@Override
	@Transactional
	public void resetDeckProgress(Long deckId) {
		var profile = this.authService.getCurrentProfile();
		this.userCardSrsRepository.deleteByProfileIdAndCardDeckId(profile.getId(), deckId);
	}
}
