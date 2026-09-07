package com.fivetuat.fsoftprojectserver.dtos.response;

import com.fivetuat.fsoftprojectserver.enums.CommunityEventType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommunityEventPayload<T> {
    CommunityEventType type;
    Long postId;
    T data;

    @Builder.Default
    LocalDateTime timestamp = LocalDateTime.now();
}
