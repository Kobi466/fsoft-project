package com.fivetuat.fsoftprojectserver.jobs;

import com.fivetuat.fsoftprojectserver.services.DeckService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PurgeDeletedDecksJob {

    DeckService deckService;

    /**
     * Chạy tự động mỗi ngày lúc 02:00 sáng để dọn dẹp vĩnh viễn các bộ bài đã đánh dấu soft-delete.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void purgeDeletedDecks() {
        log.info("Khởi chạy Scheduled Job: PurgeDeletedDecksJob dọn dẹp các bộ bài soft-deleted...");
        try {
            deckService.purgeSoftDeletedDecks();
        } catch (Exception e) {
            log.error("Lỗi khi thực thi Scheduled Job dọn dẹp bộ bài bị xóa: {}", e.getMessage(), e);
        }
    }
}
