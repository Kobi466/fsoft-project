package com.fivetuat.fsoftprojectserver.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FolderName {
    AVATARS("avatars"),
    PRODUCTS("products"),
    DECKS("decks"),
    ATTACHMENTS("attachments"),
    Card("cards");
    private final String path;
}
