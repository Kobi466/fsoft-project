package com.fivetuat.fsoftprojectserver.dtos.https.ai.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VocabLookupRequest {

    @NotBlank(message = "Từ vựng cần tra không được để trống")
    @Size(max = 64, message = "Từ vựng không được vượt quá 64 ký tự")
    String word;

    String context;

    @JsonProperty("allowed_deck_ids")
    List<Long> allowedDeckIds;
}
