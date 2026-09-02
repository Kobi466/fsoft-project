package com.fivetuat.fsoftprojectserver.dtos.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminDashboardStatsResponse {
    long totalUsers;
    long totalDecks;
    long totalCards;
    long totalGameRecords;
    long totalOfficialDecks;
    long totalPublicDecks;
}
