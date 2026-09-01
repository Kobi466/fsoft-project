package com.fivetuat.fsoftprojectserver.dtos.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommunityPostUpdateRequest {

    @Size(max = 255, message = "Tiêu đề bài viết không được vượt quá 255 ký tự")
    String title;

    String content;

    Long deckId;

    Set<Long> tagIds;
}
