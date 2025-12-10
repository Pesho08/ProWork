package com.prowork;

import com.prowork.model.*;
import javafx.scene.web.WebEngine;
import java.time.LocalDate;
import java.util.List;

public class JavaBridge {
  private final WebEngine engine;
  private final TaskManager taskManager;

  public JavaBridge(WebEngine engine) { 
    this.engine = engine;
    this.taskManager = new TaskManager();
    // Test-Daten entfernt - Tasks können über UI erstellt werden
  }

  // Task hinzufügen
  public String addTask(String name, String deadline, String type, String priority, String repetition) {
    try {
      Task task = new Task(
        name,
        LocalDate.parse(deadline),
        TaskType.valueOf(type),
        Priority.valueOf(priority),
        RepetitionPattern.valueOf(repetition)
      );
      taskManager.addTask(task);
      System.out.println("Task added with ID: " + task.getId());
      return task.getId();
    } catch (Exception e) {
      System.err.println("Error adding task: " + e.getMessage());
      return null;
    }
  }

  // All Tasks as JSON-String 
  public String getAllTasks() {
    List<Task> tasks = taskManager.getAllTasks();
    System.out.println("Getting all tasks. Count: " + tasks.size());
    return tasksToJson(tasks);
  }

  // Delete Task
  public boolean deleteTask(String id) {
    System.out.println("=== DELETE TASK CALLED ===");
    System.out.println("ID to delete: '" + id + "'");
    System.out.println("ID type: " + (id != null ? id.getClass().getName() : "null"));
    System.out.println("ID length: " + (id != null ? id.length() : "null"));
    
    // Liste alle Tasks vor dem Löschen
    List<Task> allTasks = taskManager.getAllTasks();
    System.out.println("Tasks before delete: " + allTasks.size());
    for (Task task : allTasks) {
      System.out.println("  - Task ID: '" + task.getId() + "' Name: " + task.getName());
      System.out.println("    IDs equal? " + task.getId().equals(id));
    }
    
    boolean result = taskManager.deleteTask(id);
    
    System.out.println("Delete result: " + result);
    System.out.println("Tasks after delete: " + taskManager.getAllTasks().size());
    System.out.println("=========================");
    
    return result;
  }

  // Task as done mark
  public boolean completeTask(String id) {
    Task task = taskManager.getTask(id);
    if (task != null) {
      task.setCompleted(true);
      return true;
    }
    return false;
  }

  // Tasks for a specific date
  public String getTasksForDate(String dateStr) {
    try {
      LocalDate date = LocalDate.parse(dateStr);
      List<Task> tasks = taskManager.getTasksForDate(date);
      return tasksToJson(tasks);
    } catch (Exception e) {
      System.err.println("Error getting tasks for date: " + e.getMessage());
      return "[]";
    }
  }

  // Notes just for Tasks of Type TEST
  public boolean setTaskNotes(String id, String notes) {
    Task task = taskManager.getTask(id);
    if (task != null && task.canHaveNotes()) {
      task.setNotes(notes);
      return true;
    }
    return false;
  }

  // Tasks Details as JSON
  public String getTask(String id) {
    Task task = taskManager.getTask(id);
    if (task != null) {
      return taskToJson(task);
    }
    return null;
  }

  // Hilfsmethode: Tasks to JSON convert
  private String tasksToJson(List<Task> tasks) {
    StringBuilder json = new StringBuilder("[");
    for (int i = 0; i < tasks.size(); i++) {
      json.append(taskToJson(tasks.get(i)));
      if (i < tasks.size() - 1) {
        json.append(",");
      }
    }
    json.append("]");
    return json.toString();
  }

  // A Tasks to JSON
  private String taskToJson(Task task) {
    return String.format(
      "{\"id\":\"%s\",\"name\":\"%s\",\"deadline\":\"%s\",\"type\":\"%s\",\"priority\":\"%s\",\"repetition\":\"%s\",\"notes\":\"%s\",\"completed\":%b,\"color\":\"%s\"}",
      task.getId(),
      escapeJson(task.getName()),
      task.getDeadline().toString(),
      task.getType().name(),
      task.getPriority().name(),
      task.getRepetition().name(),
      escapeJson(task.getNotes()),
      task.isCompleted(),
      task.getType().getColor()
    );
  }

  // Json escape
  private String escapeJson(String str) {
    if (str == null) return "";
    return str.replace("\\", "\\\\")
              .replace("\"", "\\\"")
              .replace("\n", "\\n")
              .replace("\r", "\\r");
  }
}