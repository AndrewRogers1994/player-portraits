package com.mmo.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TextScale {
    SMALL(0.8f),
    MEDIUM(1.0f),
    LARGE(1.2f);
    
    private final float multiplier;
}
