package com.fivetuat.fsoftprojectserver.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminBanUserRequest {

    @NotBlank(message = "Lý do khóa tài khoản không được để trống")
    String reason;

    Integer banDurationDays;
}