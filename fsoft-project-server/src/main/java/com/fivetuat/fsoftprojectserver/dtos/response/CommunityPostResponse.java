package com.fivetuat.fsoftprojectserver.dtos.response;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.fivetuat.fsoftprojectserver.enums.PostStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommunityPostResponse {

    Long id;

    UUID profileId;
    String profileName;
    String profileAvatar;

    String title;
    String content;
    PostStatus status;
    String rejectionReason;
    LocalDateTime approvedAt;

    int likeCount;
    int commentCount;
    boolean likedByCurrentUser;

    Long deckId;
    String deckTitle;
    String deckDescription;
    String deckCoverImageUrl;
    int deckTotalCards;

    Set<TagResponse> tags;

    String attachmentUrl;
    String attachmentName;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
