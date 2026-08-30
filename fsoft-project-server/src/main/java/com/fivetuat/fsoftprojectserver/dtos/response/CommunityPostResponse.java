package com.fivetuat.fsoftprojectserver.dtos.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommunityPostResponse {

    Long id;

    // Tác giả
    UUID profileId;
    String profileName;
    String profileAvatar;

    // Nội dung bài viết
    String title;
    String content;
    int likeCount;
    int commentCount;
    boolean likedByCurrentUser;

    // Deck đính kèm (nếu có)
    Long deckId;
    String deckTitle;
    String deckDescription;
    String deckCoverImageUrl;
    int deckTotalCards;

    // Danh sách nhãn
    Set<TagResponse> tags;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
