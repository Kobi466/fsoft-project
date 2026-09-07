package com.fivetuat.fsoftprojectserver.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Schema(description = "Request xác nhận/hủy hành động AI đã soạn")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminAiConfirmRequest {

    @NotBlank(message = "actionId không được để trống")
    @Schema(description = "ID hành động nhận được từ /chat khi có pendingAction", example = "9b2f6a1e-...")
    String actionId;
}
