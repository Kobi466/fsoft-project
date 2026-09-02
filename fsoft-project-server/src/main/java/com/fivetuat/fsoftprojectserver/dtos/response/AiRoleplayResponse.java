package com.fivetuat.fsoftprojectserver.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Kết quả phản hồi đối thoại nhập vai và đánh giá từ Gia Sư AI")
public class AiRoleplayResponse {
    @Schema(description = "Câu đáp lại tiếp theo của Gia sư AI trong kịch bản (Tiếng Anh tự nhiên)", example = "Great, it sounds like you value teamwork. Can you tell me about a time when you had to adjust the deadline?")
    String tutorReply;

    @Schema(description = "Điểm số đánh giá lượt nói của học viên (Thang điểm 0 - 100)", example = "95")
    int score;

    @Schema(description = "Danh sách các từ vựng mục tiêu mà học viên đã lồng ghép sử dụng thành công", example = "[\"deadline\", \"consensus\"]")
    List<String> wordsUsed;

    @Schema(description = "Danh sách các lời khuyên, nhận xét và sửa lỗi chính tả/ngữ pháp bằng Tiếng Việt", example = "[\"Chú ý dùng dạng danh từ số nhiều cho 'code reviews'\", \"Có thể thêm liên từ 'by' để câu mạch lạc hơn\"]")
    List<String> suggestions;
}
