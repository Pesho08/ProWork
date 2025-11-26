package com.prowork.model;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

public class Task {
    private String id;
    private String name;
    private LocalDate deadline;
    private TaskType type;
    private Priority priority;
    private RepetitionPattern repetition;
    private String notes; // Nur für Tests
    private boolean completed;
    private LocalDateTime completedAt;

    // Constructor
    public Task(String name, LocalDate deadline, TaskType type, Priority priority, RepetitionPattern repetition) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.deadline = deadline;
        this.type = type;
        this.priority = priority;
        this.repetition = repetition;
        this.notes = "";
        this.completed = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public TaskType getType() { return type; }
    public void setType(TaskType type) { this.type = type; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public RepetitionPattern getRepetition() { return repetition; }
    public void setRepetition(RepetitionPattern repetition) { this.repetition = repetition; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { 
      this.completed = completed; 
      if (completed) {
          this.completedAt = LocalDateTime.now();
      }
    }

    public LocalDateTime getCompletedAt() { return completedAt; }

    // Method to check if notes can be added
    public boolean canHaveNotes() {
      return type == TaskType.TEST;
    }

    // Method to check if the task is repeating
    public boolean isRepeating() {
      return repetition != RepetitionPattern.NONE;
    }
  }
