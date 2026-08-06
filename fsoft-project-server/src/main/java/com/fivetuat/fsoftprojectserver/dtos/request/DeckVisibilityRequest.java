package com.fivetuat.fsoftprojectserver.dtos.request;

import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeckVisibilityRequest {
    private DeckVisibility visibility;
}