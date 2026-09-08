package com.fivetuat.fsoftprojectserver.jobs;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fivetuat.fsoftprojectserver.services.CommunityPostService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PurgeDeletedCommunityPostsJob {

    CommunityPostService communityPostService;

    @Scheduled(cron = "0 30 2 * * ?")
    public void purgeDeletedPostsAndComments() {
        log.info("Khởi chạy Scheduled Job: PurgeDeletedCommunityPostsJob dọn dẹp bài viết & bình luận soft-deleted...");
        try {
            communityPostService.purgeSoftDeletedPostsAndComments();
        } catch (Exception e) {
            log.error("Lỗi khi thực thi Scheduled Job dọn dẹp bài viết & bình luận: {}", e.getMessage(), e);
        }
    }
}