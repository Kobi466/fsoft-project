package com.fivetuat.fsoftprojectserver.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentCreateRequest {

    @NotBlank(message = "Nội dung bình luận không được để trống")
    String content;

    Long parentCommentId;
}
