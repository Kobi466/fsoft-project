package com.fivetuat.fsoftprojectserver.dtos.https.ai.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiChatRequest {
    @JsonProperty("query")
    String query;
    @JsonProperty("allowed_deck_ids")
    List<Long> allowedDeckIds;
    @JsonProperty("scope_deck_id")
    Long scopeDeckId;
    @JsonProperty("history")
    List<ChatMessage> history;
    @JsonProperty("options")
    ChatOptions options;
}
