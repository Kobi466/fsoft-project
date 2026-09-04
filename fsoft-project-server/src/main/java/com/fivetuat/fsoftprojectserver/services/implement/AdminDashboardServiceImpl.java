package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.response.AdminDashboardStatsResponse;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import com.fivetuat.fsoftprojectserver.repositories.AuthRepository;
import com.fivetuat.fsoftprojectserver.repositories.CardRepository;
import com.fivetuat.fsoftprojectserver.repositories.DeckRepository;
import com.fivetuat.fsoftprojectserver.repositories.GameRecordRepository;
import com.fivetuat.fsoftprojectserver.services.AdminDashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    AuthRepository authRepository;
    DeckRepository deckRepository;
    CardRepository cardRepository;
    GameRecordRepository gameRecordRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "admin_dashboard_stats", key = "'stats'")
    public AdminDashboardStatsResponse getDashboardStats() {
        long totalUsers = authRepository.count();
        long totalDecks = deckRepository.count();
        long totalCards = cardRepository.count();
        long totalGameRecords = gameRecordRepository.count();
        
        long totalOfficialDecks = deckRepository.countByIsOfficialTrueAndIsDeletedFalse();
        long totalPublicDecks = deckRepository.countByVisibilityAndIsDeletedFalse(DeckVisibility.PUBLIC);

        return AdminDashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalDecks(totalDecks)
                .totalCards(totalCards)
                .totalGameRecords(totalGameRecords)
                .totalOfficialDecks(totalOfficialDecks)
                .totalPublicDecks(totalPublicDecks)
                .build();
    }
}
