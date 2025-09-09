package com.mmo.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.*;

@Getter
@RequiredArgsConstructor
public enum BarContent {
    OFF("Off", 0, 0, 0, 0, "", new Color(0, 0, 0, 0)),
    HP("Health Points", 127, 18, 18, 2, "hp_icon.png", new Color(225, 35, 0, 125)),
    PRAYER("Prayer Points", 127, 18, 18, 2, "prayer_icon.png", new Color(0, 100, 200, 125)),
    SPECIAL("Special Attack", 127, 18, 18, 2, "spec_icon.png", new Color(0, 150, 0, 125));
    
    private final String displayName;
    private final int width;
    private final int height;
    private final int fontSize;
    private final int textYOffset;
    private final String iconPath;
    private final Color barColor;
}
