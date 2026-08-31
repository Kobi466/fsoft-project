package com.fivetuat.fsoftprojectserver.dtos.request;

import com.fivetuat.fsoftprojectserver.enums.PostStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostModerateRequest {

    @NotNull(message = "Trạng thái kiểm duyệt không được để trống")
    PostStatus status;

    String reason;
}
