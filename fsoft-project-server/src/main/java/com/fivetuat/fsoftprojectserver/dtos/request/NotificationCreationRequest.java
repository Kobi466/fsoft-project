package com.fivetuat.fsoftprojectserver.dtos.request;

import com.fivetuat.fsoftprojectserver.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationCreationRequest {
    @NotNull(message = "ID người nhận không được để trống")
    UUID recipientId;

    @NotBlank(message = "Tiêu đề thông báo không được để trống")
    String title;

    @NotBlank(message = "Nội dung thông báo không được để trống")
    String content;

    @NotNull(message = "Loại thông báo không được để trống")
    NotificationType type;

    String referenceId;
}
