package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminGameRecordResponse;
import com.fivetuat.fsoftprojectserver.entities.GameRecord;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.repositories.GameRecordRepository;
import com.fivetuat.fsoftprojectserver.services.AdminGameService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminGameServiceImpl implements AdminGameService {

    GameRecordRepository gameRecordRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminGameRecordResponse> getAllGameRecords(String gameType, Long deckId, Pageable pageable) {
        Page<GameRecord> recordPage = gameRecordRepository.findAll(pageable);

        List<AdminGameRecordResponse> content = recordPage.getContent().stream()
                .map(this::mapToAdminGameRecordResponse)
                .collect(Collectors.toList());

        return PageResponse.<AdminGameRecordResponse>builder()
                .content(content)
                .totalElements(recordPage.getTotalElements())
                .totalPages(recordPage.getTotalPages())
                .pageNo(recordPage.getNumber())
                .pageSize(recordPage.getSize())
                .last(recordPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public void deleteGameRecord(String recordId) {
        GameRecord record = getExistingGameRecord(recordId);
        gameRecordRepository.delete(record);
    }

    // --- REUSABLE HELPER METHODS (DRY) ---

    private GameRecord getExistingGameRecord(String recordId) {
        return gameRecordRepository.findById(recordId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
    }

    private AdminGameRecordResponse mapToAdminGameRecordResponse(GameRecord record) {
        String userEmail = record.getUser() != null ? record.getUser().getEmail() : null;
        String userFullName = (record.getUser() != null && record.getUser().getProfile() != null)
                ? record.getUser().getProfile().getFullName() : null;
        String deckTitle = record.getDeck() != null ? record.getDeck().getTitle() : null;
        Long deckId = record.getDeck() != null ? record.getDeck().getId() : null;

        int total = record.getTotalQuestions() != null ? record.getTotalQuestions() : 0;
        int correct = record.getCorrectAnswers() != null ? record.getCorrectAnswers() : 0;
        double accuracy = total > 0 ? (double) correct / total * 100 : 0;

        LocalDateTime createdDateTime = record.getCreatedAt() != null
                ? LocalDateTime.ofInstant(record.getCreatedAt(), ZoneId.systemDefault()) : null;

        return AdminGameRecordResponse.builder()
                .id(record.getId())
                .gameType(record.getGameType())
                .score(record.getScore())
                .totalCards(total)
                .correctCount(correct)
                .timeInSeconds(record.getCompletionTime() != null ? record.getCompletionTime().intValue() : 0)
                .accuracyRate(accuracy)
                .deckId(deckId)
                .deckTitle(deckTitle)
                .userEmail(userEmail)
                .userFullName(userFullName)
                .createdAt(createdDateTime)
                .build();
    }
}
