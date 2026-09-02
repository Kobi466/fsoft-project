package com.fivetuat.fsoftprojectserver.dtos.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUpdateUserRolesRequest {
    @NotEmpty(message = "Role names cannot be empty")
    Set<String> roleNames;
}
