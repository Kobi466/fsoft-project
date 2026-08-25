package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminGameRecordResponse;
import org.springframework.data.domain.Pageable;

public interface AdminGameService {
    PageResponse<AdminGameRecordResponse> getAllGameRecords(String gameType, Long deckId, Pageable pageable);
    void deleteGameRecord(String recordId);
}
