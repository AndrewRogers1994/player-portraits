package com.mmo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.*;

@Getter
@RequiredArgsConstructor
public enum BarType {
    BAR1( 126 + 10,80, 18, 7, BarDirection.ORIGINAL),
    BAR2(108 + 10, 99, 18, 7, BarDirection.ORIGINAL),
    BAR3(130 + 10,78, 18, 7, BarDirection.ORIGINAL),
    TARGET_BAR1(126 + 10,0, 18, 7, BarDirection.INVERTED);


    private final int width;
    private final int x;
    private final int fontSize;
    private final int textYOffset;
    private final BarDirection barDirection;

}


