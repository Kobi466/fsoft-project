package com.fivetuat.fsoftprojectserver.dtos.https.ai.request;

import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Request tạo bài học tình huống (Situational Learning)")
public class SituationalLearningRequest {
    @NotBlank(message = "Tình huống không được để trống")
    @Schema(description = "Mô tả tình huống cần học (Bằng tiếng Anh hoặc tiếng Việt). Ví dụ: 'Trễ chuyến bay ở sân bay', 'Đi phỏng vấn xin việc'", example = "Sáng nay tôi mới dậy lúc 6h30 và đánh răng rửa mặt...")
    String context;

    @NotBlank(message = "Trình độ không được để trống")
    @Schema(description = "Trình độ người học hiện tại", example = "A1 - Beginner")
    String cefrLevel;
    
    @Schema(description = "Thời gian hiện tại của thiết bị để AI hiểu bối cảnh. Ví dụ: '2026-08-25T11:31:35+07:00'", example = "2026-08-25T11:31:35+07:00")
    String currentTime;
    
    @Schema(description = "Vị trí hiện tại của thiết bị (Tùy chọn) để AI cá nhân hóa bài học. Ví dụ: 'Hanoi, Vietnam'", example = "Hanoi, Vietnam")
    String location;
}
