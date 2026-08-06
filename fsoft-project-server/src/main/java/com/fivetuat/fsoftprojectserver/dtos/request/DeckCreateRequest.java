package com.fivetuat.fsoftprojectserver.dtos.request;

import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
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

    boolean isOfficial;

    java.util.Set<Long> tagIds;
}
