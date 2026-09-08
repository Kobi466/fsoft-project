package com.fivetuat.fsoftprojectserver.dtos.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUserResponse {
    UUID id;
    String email;
    String phone;
    String fullName;
    String avatar;
    Set<String> roles;
    LocalDateTime createTime;
    LocalDateTime updateTime;
    boolean isBanned;
    LocalDateTime bannedAt;
    LocalDateTime bannedUntil;
    String banReason;
}
