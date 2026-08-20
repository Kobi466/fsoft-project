package com.fivetuat.fsoftprojectserver.dtos.https.ai.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatOptions {
    @JsonProperty("top_k")
    Integer topK;
    @JsonProperty("max_output_tokens")
    Integer maxOutputTokens;
}
