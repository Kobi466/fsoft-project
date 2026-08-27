package com.fivetuat.fsoftprojectserver.dtos.response;
import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Gợi ý tự động (Autocomplete Suggestions) khi người dùng vừa gõ từ vựng")
public class CardSuggestionResponse {
	@Schema(description = "Từ vựng / Cụm từ đang được truy vấn", example = "meet")
	String query;
	@Schema(description = "Danh sách gợi ý hoàn thiện từ vựng Tiếng Anh (Gợi ý dưới ô TERM)", example = "[\"meet\", \"meeting\", \"meet, met, met\"]")
	List<String> termSuggestions;
	@Schema(description = "Danh sách gợi ý nghĩa Tiếng Việt tương ứng (Gợi ý dưới ô DEFINITION)", example = "[\"gặp\", \"đáp ứng\", \"gặp gỡ\"]")
	List<String> definitionSuggestions;
	@Schema(description = "Thông tin đầy đủ của thẻ bài gợi ý tốt nhất để tự động điền các trường còn lại (IPA, Từ loại, Ví dụ)")
	CardCreationRequest autoFillCard;
}
