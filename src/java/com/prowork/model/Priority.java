package com.prowork.model;

public enum Priority {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low"),
    NOT_USED("Not Used");

    private final String displayName;

    Priority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}