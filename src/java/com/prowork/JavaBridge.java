package com.prowork;

import com.prowork.model.*;
import javafx.scene.web.WebEngine;
import java.time.LocalDate;
import java.util.List;

/**
 * Bridge between JavaScript frontend and Java backend.
 * 
 * This class is injected into the WebView's JavaScript context, allowing
 * JavaScript code to call Java methods directly. It handles all communication
 * between the HTML/JS frontend and the Java task management backend.
 * 
 * The JavaBridge uses a static TaskManager instance shared across all
 * JavaBridge instances, ensuring data consistency when switching views.
 * 
 * @author Chris
 * @version 1.0
 */
public class JavaBridge {
  private final WebEngine engine;
  
  /**
   * Static TaskManager shared between all JavaBridge instances.
   * This ensures tasks persist when switching between views.
   */
  private static final TaskManager taskManager = new TaskManager();

  /**
   * Constructs a JavaBridge for the given WebEngine.
   * 
   * @param engine The WebEngine to control for view switching
   */
  public JavaBridge(WebEngine engine) { 
    this.engine = engine;
  }

  /**
   * Adds a new task to the system.
   * Called from JavaScript via javaBridge.addTask(...).
   * 
   * @param name The task name
   * @param type The task type (TEST, HOMEWORK, MEETING, TRAINING, WORK)
   * @param priority The priority level (HIGH, MEDIUM, LOW)
   * @param dueDate The due date in YYYY-MM-DD format
   * @param repetition The repetition pattern (NONE, DAILY, WEEKLY, MONTHLY, YEARLY)
   * @param notes Optional notes (can be empty)
   * @return The ID of the created task, or null if creation failed
   */
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

  /**
   * Retrieves all tasks as a JSON string.
   * Called from JavaScript via javaBridge.getAllTasks().
   * 
   * The JSON format uses 'dueDate' and 'taskType' as field names to match
   * the JavaScript frontend expectations.
   * 
   * @return JSON array of all tasks
   */
  public String getAllTasks() {
    List<Task> tasks = taskManager.getAllTasks();
    System.out.println("Getting all tasks. Count: " + tasks.size());
    return tasksToJson(tasks);
  }

  /**
   * Deletes a task by ID.
   * Called from JavaScript via javaBridge.deleteTask(id).
   * 
   * @param id The ID of the task to delete
   * @return true if deletion was successful, false otherwise
   */
  public boolean deleteTask(String id) {
    System.out.println("Deleting task: " + id);
    boolean result = taskManager.deleteTask(id);
    System.out.println("Delete result: " + result);
    return result;
  }

  /**
   * Marks a task as completed.
   * Called from JavaScript via javaBridge.completeTask(id).
   * 
   * @param id The ID of the task to mark as complete
   * @return true if task was found and marked complete, false otherwise
   */
  public boolean completeTask(String id) {
    Task task = taskManager.getTask(id);
    if (task != null) {
      task.setCompleted(true);
      taskManager.save(); // Persist the change
      System.out.println("Task completed: " + id);
      return true;
    }
    return false;
  }

  /**
   * Retrieves all tasks for a specific date as JSON.
   * Called from JavaScript via javaBridge.getTasksForDate(dateStr).
   * 
   * @param dateStr The date in YYYY-MM-DD format
   * @return JSON array of tasks for that date
   */
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

  /**
   * Updates the notes for a task.
   * Called from JavaScript via javaBridge.updateTaskNotes(id, notes).
   * Only works for tasks that support notes (TEST type).
   * 
   * @param id The ID of the task
   * @param notes The new notes text
   * @return true if notes were updated, false if task doesn't support notes
   */
  public boolean updateTaskNotes(String id, String notes) {
    System.out.println("Updating notes for task: " + id);
    Task task = taskManager.getTask(id);
    if (task != null && task.canHaveNotes()) {
      task.setNotes(notes);
      taskManager.save(); // Persist the change
      System.out.println("Notes updated successfully");
      return true;
    }
    System.out.println("Could not update notes");
    return false;
  }

  /**
   * Retrieves a single task as JSON.
   * Called from JavaScript via javaBridge.getTask(id).
   * 
   * @param id The ID of the task to retrieve
   * @return JSON representation of the task, or null if not found
   */
  public String getTask(String id) {
    Task task = taskManager.getTask(id);
    if (task != null) {
      return taskToJson(task);
    }
    return null;
  }

  /**
   * Switches the view to the calendar display.
   * Called from JavaScript via javaBridge.switchToCalendar().
   * Loads calendar.html into the WebView.
   */
  public void switchToCalendar() {
    System.out.println("Switching to calendar view...");
    try {
      String calendarPath = getClass().getResource("/calendar.html").toExternalForm();
      engine.load(calendarPath);
    } catch (Exception e) {
      System.err.println("Error switching to calendar: " + e.getMessage());
    }
  }

  /**
   * Switches the view to the task list display.
   * Called from JavaScript via javaBridge.switchToTaskList().
   * Loads index.html into the WebView.
   */
  public void switchToTaskList() {
    System.out.println("Switching to task list view...");
    try {
      String indexPath = getClass().getResource("/index.html").toExternalForm();
      engine.load(indexPath);
    } catch (Exception e) {
      System.err.println("Error switching to task list: " + e.getMessage());
    }
  }

  /**
   * Converts a list of tasks to JSON array format.
   * 
   * @param tasks The list of tasks to convert
   * @return JSON array string
   */
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

  /**
   * Converts a single task to JSON format.
   * 
   * Note: Uses 'dueDate' and 'taskType' as field names instead of 'deadline' 
   * and 'type' to match JavaScript frontend expectations.
   * 
   * @param task The task to convert
   * @return JSON object string
   */
  private String taskToJson(Task task) {
    return String.format(
      "{\"id\":\"%s\",\"name\":\"%s\",\"dueDate\":\"%s\",\"taskType\":\"%s\",\"priority\":\"%s\",\"repetition\":\"%s\",\"notes\":\"%s\",\"completed\":%b,\"color\":\"%s\"}",
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

  /**
   * Escapes special characters for JSON format.
   * Handles quotes, backslashes, newlines, and carriage returns.
   * 
   * @param str The string to escape
   * @return Escaped string safe for JSON
   */
  private String escapeJson(String str) {
    if (str == null) return "";
    return str.replace("\\", "\\\\")
              .replace("\"", "\\\"")
              .replace("\n", "\\n")
              .replace("\r", "\\r");
  }
}