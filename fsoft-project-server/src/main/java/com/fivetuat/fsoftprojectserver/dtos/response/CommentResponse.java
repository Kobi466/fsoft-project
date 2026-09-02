package com.fivetuat.fsoftprojectserver.dtos.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {

    Long id;
    Long communityPostId;

    // Người bình luận
    UUID profileId;
    String profileName;
    String profileAvatar;

    // Reply cha (nếu có)
    Long parentCommentId;

    String content;

    // Số lượng câu trả lời con (Replies) của bình luận này (Dùng để hiển thị: "↳ 3 phản hồi")
    int replyCount;

    // Danh sách câu trả lời con (Khi gọi API lấy replies)
    List<CommentResponse> replies;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
