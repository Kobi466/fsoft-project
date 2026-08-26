package com.fivetuat.fsoftprojectserver.dtos.request;

import java.util.Set;

import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class DeckCreateRequest {
    @NotBlank(message = "Title cannot be blank")
    String title;

    String description;

    Long parentDeckId;

    @NotBlank(message = "Source language cannot be blank")
    @Builder.Default
    String sourceLanguage = "en";

    @NotBlank(message = "Target language cannot be blank")
    @Builder.Default
    String targetLanguage = "vi";

    @NotNull(message = "Visibility cannot be null")
    DeckVisibility visibility;

    String coverImageUrl;

    Set<Long> tagIds;
}
