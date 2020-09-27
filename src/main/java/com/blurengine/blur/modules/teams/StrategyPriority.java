package com.blurengine.blur.modules.teams;

public enum StrategyPriority {
    HIGHEST(0),
    HIGH(1),
    NORMAL(2),
    LOWEST(3),
    LOW(4);

    private final int slot;

    private StrategyPriority(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return this.slot;
    }
}
