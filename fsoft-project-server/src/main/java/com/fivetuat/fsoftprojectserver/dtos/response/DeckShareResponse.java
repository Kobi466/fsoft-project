package com.fivetuat.fsoftprojectserver.dtos.response;

import com.fivetuat.fsoftprojectserver.enums.SharePermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class DeckShareResponse {
	UUID profileId;
	String email;
	String fullName;
	String avatar;
	SharePermission permission;
}