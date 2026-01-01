package com.prowork.model;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a single task in the ProWork task management system.
 * 
 * A task has a name, deadline, type, priority, and optional repetition pattern.
 * Tasks can be marked as completed and may have associated notes (for TEST type tasks).
 * Each task has a unique ID generated automatically.
 * 
 * @author Chris
 * @version 1.0
 */
public class Task {
    private String id;
    private String name;
    private LocalDate deadline;
    private TaskType type;
    private Priority priority;
    private RepetitionPattern repetition;
    private String notes;
    private boolean completed;
    private LocalDateTime completedAt;

    /**
     * Constructs a new Task with the specified parameters.
     * 
     * @param name The name/title of the task
     * @param deadline The due date for the task
     * @param type The type of task (TEST, HOMEWORK, MEETING, etc.)
     * @param priority The priority level (HIGH, MEDIUM, LOW)
     * @param repetition The repetition pattern (NONE, DAILY, WEEKLY, etc.)
     */
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

    /**
     * Gets the unique identifier of this task.
     * 
     * @return The task ID
     */
    public String getId() { return id; }
    
    /**
     * Sets the unique identifier of this task.
     * This is primarily used when loading tasks from storage.
     * 
     * @param id The task ID to set
     */
    public void setId(String id) { this.id = id; }

    /**
     * Gets the name of this task.
     * 
     * @return The task name
     */
    public String getName() { return name; }
    
    /**
     * Sets the name of this task.
     * 
     * @param name The new task name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Gets the deadline for this task.
     * 
     * @return The task deadline
     */
    public LocalDate getDeadline() { return deadline; }
    
    /**
     * Sets the deadline for this task.
     * 
     * @param deadline The new deadline
     */
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    /**
     * Gets the type of this task.
     * 
     * @return The task type
     */
    public TaskType getType() { return type; }
    
    /**
     * Sets the type of this task.
     * 
     * @param type The new task type
     */
    public void setType(TaskType type) { this.type = type; }

    /**
     * Gets the priority level of this task.
     * 
     * @return The priority level
     */
    public Priority getPriority() { return priority; }
    
    /**
     * Sets the priority level of this task.
     * 
     * @param priority The new priority level
     */
    public void setPriority(Priority priority) { this.priority = priority; }

    /**
     * Gets the repetition pattern of this task.
     * 
     * @return The repetition pattern
     */
    public RepetitionPattern getRepetition() { return repetition; }
    
    /**
     * Sets the repetition pattern of this task.
     * 
     * @param repetition The new repetition pattern
     */
    public void setRepetition(RepetitionPattern repetition) { this.repetition = repetition; }

    /**
     * Gets the notes associated with this task.
     * 
     * @return The task notes
     */
    public String getNotes() { return notes; }
    
    /**
     * Sets the notes for this task.
     * 
     * @param notes The new notes text
     */
    public void setNotes(String notes) { this.notes = notes; }

    /**
     * Checks if this task is completed.
     * 
     * @return true if the task is completed, false otherwise
     */
    public boolean isCompleted() { return completed; }
    
    /**
     * Sets the completion status of this task.
     * If set to true, also records the completion timestamp.
     * 
     * @param completed The new completion status
     */
    public void setCompleted(boolean completed) { 
      this.completed = completed; 
      if (completed) {
          this.completedAt = LocalDateTime.now();
      }
    }

    /**
     * Gets the timestamp when this task was completed.
     * 
     * @return The completion timestamp, or null if not yet completed
     */
    public LocalDateTime getCompletedAt() { return completedAt; }

    /**
     * Checks if this task type allows notes.
     * Currently only TEST type tasks can have notes.
     * 
     * @return true if notes are allowed for this task type
     */
    public boolean canHaveNotes() {
      return type == TaskType.TEST;
    }

    /**
     * Checks if this task has a repetition pattern.
     * 
     * @return true if the task repeats, false if it's a one-time task
     */
    public boolean isRepeating() {
      return repetition != RepetitionPattern.NONE;
    }
}