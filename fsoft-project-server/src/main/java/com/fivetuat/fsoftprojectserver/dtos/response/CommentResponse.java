package com.fivetuat.fsoftprojectserver.dtos.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
public class CommentResponse {

    Long id;
    Long communityPostId;

    UUID profileId;
    String profileName;
    String profileAvatar;

    Long parentCommentId;

    String content;

    int replyCount;

    List<CommentResponse> replies;

    String attachmentUrl;
    String attachmentName;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
