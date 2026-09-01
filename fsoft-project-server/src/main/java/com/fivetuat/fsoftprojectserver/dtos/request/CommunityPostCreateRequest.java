package com.fivetuat.fsoftprojectserver.dtos.request;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class CommunityPostCreateRequest {

    @NotBlank(message = "Tiêu đề bài viết không được để trống")
    @Size(max = 255, message = "Tiêu đề bài viết không được vượt quá 255 ký tự")
    String title;

    @NotBlank(message = "Nội dung bài viết không được để trống")
    String content;

    Long deckId;

    Set<Long> tagIds;
}
