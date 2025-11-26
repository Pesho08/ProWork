package com.prowork.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TaskManager {
  private List<Task> tasks;

  public TaskManager() {
    this.tasks = new ArrayList<>();
  }

  // Add a new task
  public void addTask(Task task) {
    tasks.add(task);
  }

  // Remove Tasks
  public boolean deleteTask(String id) {
    return tasks.removeIf(task -> task.getId().equals(id));
  }

  // find Task

  public Task getTask(String id) {
    return tasks.stream()
        .filter(task -> task.getId().equals(id))
        .findFirst()
        .orElse(null);
  }

  // List all tasks
  public List<Task> getAllTasks() {
    return new ArrayList<>(tasks);
  }

  // Filter Tasks by Date
  public List<Task> getTasksForDate(LocalDate date) {
    return tasks.stream()
        .filter(task -> task.getDeadline().equals(date))
        .collect(Collectors.toList());
  }

  // Filter Tasks by Type
  public List<Task> getTasksByType(TaskType type) {
    return tasks.stream()
        .filter(task -> task.getType() == type)
        .collect(Collectors.toList());
  }

  // Sort Tasks
  public List<Task> getSortedTasks() {
    return tasks.stream()
        .sorted(Comparator
            .comparing(Task::getPriority)
            .thenComparing(Task::getDeadline)
            .thenComparing(Task::getName))
        .collect(Collectors.toList());
  }

  // Only Active Tasks visable
  public List<Task> getActiveTasks() {
    return tasks.stream()
        .filter(task -> !task.isCompleted())
        .collect(Collectors.toList());
  }

  // Remove completed Tasks after some time
  public void cleanupCompletedTasks(int daysOld) {
    tasks.removeIf(task ->
        task.isCompleted() &&
        !task.isRepeating() &&
        task.getCompletedAt() != null &&
        task.getCompletedAt().isBefore(LocalDateTime.now().minusDays(daysOld))
    );
  }
}