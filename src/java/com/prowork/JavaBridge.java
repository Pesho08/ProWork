package com.prowork;

import com.prowork.model.*;
import javafx.scene.web.WebEngine;
import java.time.LocalDate;
import java.util.List;

public class JavaBridge {
  private final WebEngine engine;
  // STATIC TaskManager - wird zwischen allen JavaBridge-Instanzen geteilt!
  private static final TaskManager taskManager = new TaskManager();

  public JavaBridge(WebEngine engine) { 
    this.engine = engine;
    // TaskManager wird NICHT mehr hier erstellt - verwende den static!
  }

  // Task hinzufügen
  public String addTask(String name, String type, String priority, String dueDate, String repetition, String notes) {
    try {
      Task task = new Task(
        name,
        LocalDate.parse(dueDate),
        TaskType.valueOf(type),
        Priority.valueOf(priority),
        RepetitionPattern.valueOf(repetition)
      );
      if (notes != null && !notes.isEmpty()) {
        task.setNotes(notes);
      }
      taskManager.addTask(task);
      System.out.println("Task added: " + name + " (ID: " + task.getId() + ")");
      return task.getId();
    } catch (Exception e) {
      System.err.println("Error adding task: " + e.getMessage());
      e.printStackTrace();
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
    System.out.println("Deleting task: " + id);
    boolean result = taskManager.deleteTask(id);
    System.out.println("Delete result: " + result);
    return result;
  }

  // Task as done mark
  public boolean completeTask(String id) {
    Task task = taskManager.getTask(id);
    if (task != null) {
      task.setCompleted(true);
      System.out.println("Task completed: " + id);
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

  // Update notes for a task
  public boolean updateTaskNotes(String id, String notes) {
    System.out.println("Updating notes for task: " + id);
    Task task = taskManager.getTask(id);
    if (task != null && task.canHaveNotes()) {
      task.setNotes(notes);
      System.out.println("Notes updated successfully");
      return true;
    }
    System.out.println("Could not update notes");
    return false;
  }

  // Task Details as JSON
  public String getTask(String id) {
    Task task = taskManager.getTask(id);
    if (task != null) {
      return taskToJson(task);
    }
    return null;
  }

  // Switch to calendar view
  public void switchToCalendar() {
    System.out.println("Switching to calendar view...");
    try {
      String calendarPath = getClass().getResource("/calendar.html").toExternalForm();
      engine.load(calendarPath);
    } catch (Exception e) {
      System.err.println("Error switching to calendar: " + e.getMessage());
    }
  }

  // Switch to task list view
  public void switchToTaskList() {
    System.out.println("Switching to task list view...");
    try {
      String indexPath = getClass().getResource("/index.html").toExternalForm();
      engine.load(indexPath);
    } catch (Exception e) {
      System.err.println("Error switching to task list: " + e.getMessage());
    }
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

  // A Task to JSON - MIT KORREKTEN FELDNAMEN!
  private String taskToJson(Task task) {
    return String.format(
      "{\"id\":\"%s\",\"name\":\"%s\",\"dueDate\":\"%s\",\"taskType\":\"%s\",\"priority\":\"%s\",\"repetition\":\"%s\",\"notes\":\"%s\",\"completed\":%b,\"color\":\"%s\"}",
      task.getId(),
      escapeJson(task.getName()),
      task.getDeadline().toString(),
      task.getType().name(),        // taskType (nicht type!)
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