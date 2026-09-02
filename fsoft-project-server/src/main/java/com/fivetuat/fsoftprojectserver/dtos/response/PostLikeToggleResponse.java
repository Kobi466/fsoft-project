package com.fivetuat.fsoftprojectserver.dtos.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostLikeToggleResponse {

    Long postId;

    // true nếu vừa Thích, false nếu vừa Bỏ thích
    boolean liked;

    // Tổng số lượt thích mới nhất sau khi thao tác
    int likeCount;
}
