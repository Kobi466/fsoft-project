package com.fivetuat.fsoftprojectserver.dtos.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicProfileResponse {

    UUID id;
    String fullName;
    String about;
    String avatar;

    // Các liên kết mạng xã hội
    String personalWebsite;
    String github;
    String linkedin;
    String facebook;
    String youtube;

    // Thành tích học tập
    int currentStreak;
    int longestStreak;

    // Chỉ số đóng góp cộng đồng
    long totalPosts;
    long totalPublicDecks;
}
