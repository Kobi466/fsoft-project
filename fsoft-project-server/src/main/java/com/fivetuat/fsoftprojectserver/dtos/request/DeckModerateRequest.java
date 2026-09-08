package com.fivetuat.fsoftprojectserver.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Schema(description = "Request duyệt/từ chối yêu cầu công khai bộ bài của Admin")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeckModerateRequest {

    @Schema(description = "Lý do từ chối (BẮT BUỘC khi approved=false, bị bỏ qua khi approved=true)", example = "Bộ bài chứa nội dung không phù hợp với tiêu chuẩn cộng đồng")
    String reason;
}