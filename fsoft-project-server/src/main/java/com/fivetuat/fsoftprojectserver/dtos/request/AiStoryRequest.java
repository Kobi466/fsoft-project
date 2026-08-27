package com.fivetuat.fsoftprojectserver.dtos.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiStoryRequest {
    List<Long> cardIds;
    List<String> words;
    @Builder.Default
    String contextType = "BUSINESS_EMAIL"; // BUSINESS_EMAIL, DAILY_STORY, NEWS_ARTICLE
}
