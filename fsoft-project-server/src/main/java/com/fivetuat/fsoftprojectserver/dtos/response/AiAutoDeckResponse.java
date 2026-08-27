package com.fivetuat.fsoftprojectserver.dtos.response;

import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiAutoDeckResponse {
    String title;
    String description;
    String sourceLanguage;
    String targetLanguage;
    List<CardCreationRequest> cards;
}
