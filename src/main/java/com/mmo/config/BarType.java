package com.mmo.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.*;

@Getter
@RequiredArgsConstructor
public enum BarType {
    HEALTH("Health", 127, 29, 77, 23, 18, 4, "hp_icon.png", new Color(225, 35, 0, 125)),
    PRAYER("Prayer", 90, 18, 74, 59, 14, 3, "prayer_icon.png", new Color(0, 100, 200, 125));
    
    private final String displayName;
    private final int width;
    private final int height;
    private final int x;
    private final int y;
    private final int fontSize;
    private final int textYOffset;
    private final String iconPath;
    private final Color barColor;
}
