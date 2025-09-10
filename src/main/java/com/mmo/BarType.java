package com.mmo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.*;

@Getter
@RequiredArgsConstructor
public enum BarType {
    BAR1( 126 + 10,80, 18, 7),
    BAR2(108 + 10, 99, 18, 7),
    BAR3(130 + 10,78, 18, 7);


    private final int width;
    private final int x;
    private final int fontSize;
    private final int textYOffset;

}
