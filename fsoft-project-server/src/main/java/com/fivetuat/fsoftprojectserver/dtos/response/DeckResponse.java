package com.fivetuat.fsoftprojectserver.dtos.response;

import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeckResponse {
    Long id;
    UUID profileId;
    Long parentDeckId;
    String title;
    String description;
    String coverImageUrl;
    String sourceLanguage;
    String targetLanguage;
    DeckVisibility visibility;
    boolean isOfficial;
    int totalCards;
    int forkCount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    java.util.Set<TagResponse> tags;
}
