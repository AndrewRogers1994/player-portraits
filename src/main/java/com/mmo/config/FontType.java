package com.mmo.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FontType {
    DEFAULT("Default"),
    RUNESCAPE("RuneScape"),
    RUNESCAPE_SMALL("RuneScape Small"),
    RUNESCAPE_BOLD("RuneScape Bold");
    
    private final String displayName;
}
