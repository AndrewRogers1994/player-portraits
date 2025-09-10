package com.mmo.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BadgeDisplay {
    DISABLED("Disabled"),
    COMBAT_LEVEL("Combat Level"),
    ARC_STATUS_AMOUNT("Status Amount");

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
