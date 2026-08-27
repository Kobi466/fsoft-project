package com.fivetuat.fsoftprojectserver.dtos.response;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;

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
    Set<TagResponse> tags;
    boolean isShareLinkEnabled;
    String shareToken;
}
