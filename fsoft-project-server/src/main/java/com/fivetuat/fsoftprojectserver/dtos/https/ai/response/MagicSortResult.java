package com.fivetuat.fsoftprojectserver.dtos.https.ai.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Kết quả phân loại tự động cho từng từ vựng")
public class MagicSortResult {
    @Schema(description = "Từ vựng được phân loại", example = "Laptop")
    @JsonProperty("word")
    String word;
    
    @Schema(description = "ID của Deck có sẵn phù hợp nhất. Sẽ là null nếu AI gợi ý tạo Deck mới.", example = "2")
    @JsonProperty("targetDeckId")
    Long targetDeckId;
    
    @Schema(description = "Tên Deck mới do AI gợi ý (nếu targetDeckId là null)", example = "Medical Terms")
    @JsonProperty("suggestedDeckName")
    String suggestedDeckName;
}
