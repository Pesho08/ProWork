package com.prowork.model;

public enum TaskType {
  TEST("Test", "#FF6B6B"),
  HOMEWORK("Homework", "#4ECDC4"),
  MEETING("Meeting", "#DDE66D"),
  TRAINING("Training", "#95E1D3"),    
  WORK("Work", "#A8E6CF");
  
  private final String displayName;
  private final String color; 

  TaskType(String displayName, String color) {
    this.displayName = displayName;
    this.color = color;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getColor() {
    return color;
  }
}