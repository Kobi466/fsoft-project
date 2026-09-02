package com.fivetuat.fsoftprojectserver.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fivetuat.fsoftprojectserver.enums.NotificationType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    Long id;
    UUID recipientId;
    String title;
    String content;
    NotificationType type;
    String referenceId;
    boolean isRead;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    LocalDateTime createdAt;
}
