package com.prowork.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the collection of tasks and provides operations for task manipulation.
 * 
 * This class handles all CRUD operations for tasks and automatically persists
 * changes to disk using the TaskPersistence class. Tasks are loaded from disk
 * on initialization and saved after every modification.
 * 
 * @author Chris
 * @version 1.0
 */
public class TaskManager {
  private List<Task> tasks;
  private TaskPersistence persistence;

  /**
   * Constructs a new TaskManager and loads existing tasks from disk.
   * If no tasks file exists, starts with an empty list.
   */
  public TaskManager() {
    this.persistence = new TaskPersistence();
    this.tasks = persistence.loadTasks();
    System.out.println("TaskManager initialized with " + tasks.size() + " tasks");
  }

  /**
   * Adds a new task to the manager and persists to disk.
   * 
   * @param task The task to add
   */
  public void addTask(Task task) {
    tasks.add(task);
    save();
  }

  /**
   * Deletes a task by ID and persists the change to disk.
   * 
   * @param id The ID of the task to delete
   * @return true if task was found and deleted, false otherwise
   */
  public boolean deleteTask(String id) {
    boolean removed = tasks.removeIf(task -> task.getId().equals(id));
    if (removed) {
      save();
    }
    return removed;
  }

  /**
   * Retrieves a task by its ID.
   * 
   * @param id The ID of the task to find
   * @return The task with the given ID, or null if not found
   */
  public Task getTask(String id) {
    return tasks.stream()
        .filter(task -> task.getId().equals(id))
        .findFirst()
        .orElse(null);
  }

  /**
   * Returns a copy of all tasks.
   * 
   * @return A new ArrayList containing all tasks
   */
  public List<Task> getAllTasks() {
    return new ArrayList<>(tasks);
  }

  /**
   * Filters tasks by a specific date.
   * 
   * @param date The date to filter by
   * @return List of tasks with the given deadline
   */
  public List<Task> getTasksForDate(LocalDate date) {
    return tasks.stream()
        .filter(task -> task.getDeadline().equals(date))
        .collect(Collectors.toList());
  }

  /**
   * Filters tasks by type.
   * 
   * @param type The task type to filter by
   * @return List of tasks matching the given type
   */
  public List<Task> getTasksByType(TaskType type) {
    return tasks.stream()
        .filter(task -> task.getType() == type)
        .collect(Collectors.toList());
  }

  /**
   * Returns tasks sorted by priority, then deadline, then name.
   * 
   * @return Sorted list of tasks
   */
  public List<Task> getSortedTasks() {
    return tasks.stream()
        .sorted(Comparator
            .comparing(Task::getPriority)
            .thenComparing(Task::getDeadline)
            .thenComparing(Task::getName))
        .collect(Collectors.toList());
  }

  /**
   * Returns only tasks that are not completed.
   * 
   * @return List of active (incomplete) tasks
   */
  public List<Task> getActiveTasks() {
    return tasks.stream()
        .filter(task -> !task.isCompleted())
        .collect(Collectors.toList());
  }

  /**
   * Removes completed tasks that are older than the specified number of days.
   * Does not remove repeating tasks even if completed.
   * Persists changes to disk.
   * 
   * @param daysOld The age threshold in days
   */
  public void cleanupCompletedTasks(int daysOld) {
    boolean removed = tasks.removeIf(task ->
        task.isCompleted() &&
        !task.isRepeating() &&
        task.getCompletedAt() != null &&
        task.getCompletedAt().isBefore(LocalDateTime.now().minusDays(daysOld))
    );
    
    if (removed) {
      save();
    }
  }

  /**
   * Saves all tasks to disk using the persistence layer.
   * Called automatically after any modification.
   */
  public void save() {
    persistence.saveTasks(tasks);
  }

  /**
   * Reloads tasks from disk, discarding any unsaved changes.
   */
  public void reload() {
    this.tasks = persistence.loadTasks();
    System.out.println("Reloaded " + tasks.size() + " tasks from disk");
  }

  /**
   * Returns the path where tasks are persisted.
   * 
   * @return Path to the tasks file
   */
  public String getStoragePath() {
    return persistence.getTasksFilePath().toString();
  }
}