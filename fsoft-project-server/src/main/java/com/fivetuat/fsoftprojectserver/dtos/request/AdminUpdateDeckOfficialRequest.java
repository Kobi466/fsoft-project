package com.fivetuat.fsoftprojectserver.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUpdateDeckOfficialRequest {
    @NotNull(message = "isOfficial flag is required")
    Boolean isOfficial;
}
