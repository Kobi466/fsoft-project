package com.fivetuat.fsoftprojectserver.dtos.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SrsMasteryResponse {
    long totalLearnedCards;                         // Tổng thẻ đã có record trong user_card_srs
    Long newCardsCount;                             // Thẻ chưa học lần nào (null nếu không truyền deckId)
    long masteredCards;                             // Thẻ thuộc lòng (interval >= 30 ngày)
    long learningCards;                             // Thẻ đang học (LEARNING + REVIEW interval < 30)
    long lapsedCards;                               // Thẻ hay quên (status = LAPSED)
    double masteryRate;                             // % thuộc lòng, làm tròn 1 chữ số thập phân
    long dueToday;                                  // Thẻ cần ôn hôm nay (kể cả thẻ quá hạn từ hôm trước)
    List<SrsForecastDayResponse> forecastNext7Days; // Dự báo số thẻ scheduled trong 7 ngày tới
}
