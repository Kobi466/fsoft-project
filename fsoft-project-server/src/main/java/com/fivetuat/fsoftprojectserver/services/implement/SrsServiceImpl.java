package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.request.SrsReviewRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.SrsCardResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.UserActivityResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.UserStreakResponse;
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
import com.fivetuat.fsoftprojectserver.services.SrsService;
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
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SrsServiceImpl implements SrsService {
	UserCardSrsRepository userCardSrsRepository;
	CardRepository cardRepository;
	UserCardSrsMapper userCardSrsMapper;
	AuthService authService;
	CardMapper cardMapper;
	ProgressService progressService;

	@Override
	public List<SrsCardResponse> getStudyQueue(Long deckId, Integer limit) {
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
		
		List<SrsCardResponse> responses = new ArrayList<>();
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
//							SrsCardResponse.builder()
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
						SrsCardResponse.builder()
								.card(cardMapper.toCardResponse(card))
								.status(SrsStatus.NEW)
								.repetitions(0)
								.interval(0)
								.easinessFactor(2.5f)
								.build()
				);
			}
		}
		return responses;
	}

	/**
	 * Thực hiện thuật toán SuperMemo-2 (SM-2) để tính toán lịch ôn tập tiếp theo cho flashcard.
	 *
	 * <p><b>1. Thang điểm đánh giá (Quality Grade - q):</b></p>
	 * Sau mỗi lần lật thẻ học, người dùng tự chấm điểm khả năng nhớ theo thang từ 0 đến 5:
	 * <ul>
	 *   <li><b>5:</b> Nhớ hoàn hảo, trả lời ngay lập tức.</li>
	 *   <li><b>4:</b> Nhớ đúng sau một chút suy nghĩ.</li>
	 *   <li><b>3:</b> Nhớ lờ mờ, khá khó khăn mới đúng.</li>
	 *   <li><b>2:</b> Quên một phần, đáp án gần đúng hoặc nhớ ra sau khi nhìn.</li>
	 *   <li><b>1:</b> Gần như quên hoàn toàn, nhưng nhớ lại được khi thấy đáp án.</li>
	 *   <li><b>0:</b> Quên sạch, hoàn toàn không nhớ gì.</li>
	 * </ul>
	 *
	 * <p><b>2. Các đại lượng cốt lõi được lưu trữ:</b></p>
	 * Mỗi thẻ học (flashcard) lưu trữ 3 thông số chính cập nhật sau mỗi lần ôn tập:
	 * <ul>
	 *   <li><b>n (Repetitions):</b> Số lần trả lời đúng liên tiếp (điểm q &gt;= 3).</li>
	 *   <li><b>EF (Easiness Factor):</b> Hệ số dễ của thẻ, khởi điểm mặc định là 2.5.</li>
	 *   <li><b>I (Interval):</b> Khoảng thời gian (tính bằng ngày) cho lần ôn tập tiếp theo.</li>
	 * </ul>
	 *
	 * <p><b>3. Quy trình tính toán thuật toán SM-2:</b></p>
	 * <ol>
	 *   <li>
	 *     <b>Cập nhật hệ số dễ (EF):</b><br>
	 *     Công thức: <code>EF' = EF + 0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)</code><br>
	 *     <i>Lưu ý:</i> Nếu EF' &lt; 1.3 thì đặt lại EF' = 1.3.
	 *   </li>
	 *   <li>
	 *     <b>Cập nhật số lần lặp lại (n):</b><br>
	 *     Nếu q &lt; 3 (trả lời sai): Đặt lại n = 0.<br>
	 *     Nếu q &gt;= 3 (trả lời đúng): Tăng thêm 1 đơn vị (n = n + 1).
	 *   </li>
	 *   <li>
	 *     <b>Tính khoảng thời gian cho lần học tiếp theo (I):</b><br>
	 *     - Nếu n = 1: I = 1 ngày.<br>
	 *     - Nếu n = 2: I = 6 ngày.<br>
	 *     - Nếu n &gt; 2: I = I_cũ * EF.
	 *   </li>
	 *   <li>
	 *     <b>Xử lý khi trả lời sai (q &lt; 3):</b><br>
	 *     Chuỗi lặp bị ngắt: Khoảng thời gian reset về I = 1 ngày. Giá trị n reset về 0,
	 *     nhưng hệ số EF hiện tại của thẻ vẫn được giữ nguyên không đổi.
	 *   </li>
	 * </ol>
	 *
	 * <p><b>Ý nghĩa thực tế:</b></p>
	 * Thuật toán giúp dãn cách thời gian biểu diễn của thẻ theo cấp số nhân khi nhớ tốt,
	 * giúp não bộ duy trì thông tin ngay trước ngưỡng quên. Các thẻ khó sẽ xuất hiện liên tục
	 * trong ngắn ngày cho tới khi thành thạo.
	 *
	 * @param q điểm số đánh giá từ người dùng (0 đến 5)
	 * @param currentN số lần lặp lại liên tiếp hiện tại
	 * @param currentEF hệ số dễ hiện tại của thẻ
	 * @param currentInterval khoảng thời gian ôn tập hiện tại (ngày)
	 */
	@Transactional
	@Override
	public SrsCardResponse reviewCard(SrsReviewRequest request) {
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

		applySm2Algorithm(userCardSrs, request.getRating());
		//ghi nhan activity va steak
		this.progressService.updateUserActivityAndStreak(profile, LocalDate.now());

		return this.userCardSrsMapper.toResponse(
				this.userCardSrsRepository.save(userCardSrs)
		);
	}

	private void applySm2Algorithm(UserCardSrs userCardSrs, int q) {
		//độ dễ mới để cập nhập
		float currentEf = userCardSrs.getEasinessFactor();
		float newEf = currentEf + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f));
		if (newEf < 1.3f) newEf = 1.3f;
		userCardSrs.setEasinessFactor(newEf);

		//số lần đúng liên tục
		int newRepetitions;
		//khoảng cách ngày ôn tập
		int newInterval;
		SrsStatus newStatus;

		if (q < 2) {
			newRepetitions = 0;
			newInterval = 1;
			newStatus = SrsStatus.LEARNING;
		} else {
			newRepetitions = userCardSrs.getRepetitions() + 1;
			if (newRepetitions == 1) {
				newInterval = 1;
			} else if (newRepetitions == 2) {
				newInterval = 6;
			} else {
				newInterval = Math.round(userCardSrs.getInterval() * newEf);
			}
			newStatus = (newInterval >= 30) ? SrsStatus.REVIEW : SrsStatus.LEARNING;
		}
		userCardSrs.setRepetitions(newRepetitions);
		userCardSrs.setInterval(newInterval);
		userCardSrs.setStatus(newStatus);
		
		if (q < 2) {
			userCardSrs.setNextReviewDate(LocalDateTime.now().plusMinutes(10));
		} else {
			userCardSrs.setNextReviewDate(LocalDateTime.now().plusDays(newInterval));
		}
	}

	@Override
	@Transactional
	public void resetDeckProgress(Long deckId) {
		var profile = this.authService.getCurrentProfile();
		this.userCardSrsRepository.deleteByProfileIdAndCardDeckId(profile.getId(), deckId);
	}
}
