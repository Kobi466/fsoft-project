package com.fivetuat.fsoftprojectserver.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TagCreateRequest {
    @NotBlank(message = "Tag name cannot be blank")
    String name;
}
