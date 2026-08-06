package com.fivetuat.fsoftprojectserver.dtos.request;

import com.fivetuat.fsoftprojectserver.enums.SharePermission;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ShareDeckRequest {

    private List<UUID> profileIds;

    private SharePermission permission;
}