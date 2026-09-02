package com.fivetuat.fsoftprojectserver.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Yêu cầu lượt đối thoại nhập vai với Gia Sư AI")
public class AiRoleplayRequest {
    @Schema(description = "Kịch bản giao tiếp (JOB_INTERVIEW, DOCTOR_APPOINTMENT, HOTEL_CHECKIN, RESTAURANT)", example = "JOB_INTERVIEW")
    @Builder.Default
    String scenario = "JOB_INTERVIEW";

    @Schema(description = "Danh sách từ vựng mục tiêu học viên cần luyện tập lồng ghép vào câu nói", example = "[\"deadline\", \"proposal\"]")
    List<String> targetWords;

    @Schema(description = "ID phiên hội thoại để duy trì bộ nhớ đa lượt (Max 36 ký tự). Nếu bỏ trống backend tự dùng User ID", example = "session-interview-101")
    String conversationId;

    @NotBlank(message = "Tin nhắn của người dùng không được để trống")
    @Schema(description = "Câu trả lời hoặc câu hỏi bằng tiếng Anh của học viên", example = "In my last project, I reached a consensus with my team to hit the deadline.")
    String userMessage;
}
