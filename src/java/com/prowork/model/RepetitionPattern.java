package com.prowork.model;

public enum RepetitionPattern {
  NONE("None"),
  DAILY("Daily"),
  WEEKLY("Weekly"),
  MONTHLY("Monthly"),
  YEARLY("Yearly");

  private final String displayName;

  RepetitionPattern(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
