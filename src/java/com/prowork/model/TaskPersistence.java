package com.prowork.model;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistence of tasks to and from JSON files.
 * 
 * This class provides methods to save and load tasks from a JSON file
 * stored in the user's home directory under .prowork/tasks.json
 * 
 * @author Chris
 * @version 1.0
 */
public class TaskPersistence {
    private static final String APP_DIR = ".prowork";
    private static final String TASKS_FILE = "tasks.json";
    private final Path tasksFilePath;

    /**
     * Constructs a new TaskPersistence instance and ensures the storage directory exists.
     * The tasks are stored in: USER_HOME/.prowork/tasks.json
     */
    public TaskPersistence() {
        String userHome = System.getProperty("user.home");
        Path appDir = Paths.get(userHome, APP_DIR);
        
        // Create directory if it doesn't exist
        try {
            if (!Files.exists(appDir)) {
                Files.createDirectories(appDir);
                System.out.println("Created ProWork directory: " + appDir);
            }
        } catch (IOException e) {
            System.err.println("Error creating ProWork directory: " + e.getMessage());
        }
        
        this.tasksFilePath = appDir.resolve(TASKS_FILE);
        System.out.println("Tasks will be stored at: " + tasksFilePath);
    }

    /**
     * Saves a list of tasks to the JSON file.
     * 
     * @param tasks The list of tasks to save
     * @return true if save was successful, false otherwise
     */
    public boolean saveTasks(List<Task> tasks) {
        try {
            String json = tasksToJson(tasks);
            Files.write(tasksFilePath, json.getBytes("UTF-8"));
            System.out.println("Successfully saved " + tasks.size() + " tasks to file");
            return true;
        } catch (IOException e) {
            System.err.println("Error saving tasks: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads tasks from the JSON file.
     * 
     * @return List of loaded tasks, or empty list if file doesn't exist or error occurs
     */
    public List<Task> loadTasks() {
        if (!Files.exists(tasksFilePath)) {
            System.out.println("No tasks file found, starting with empty task list");
            return new ArrayList<>();
        }

        try {
            String json = new String(Files.readAllBytes(tasksFilePath), "UTF-8");
            List<Task> tasks = jsonToTasks(json);
            System.out.println("Successfully loaded " + tasks.size() + " tasks from file");
            return tasks;
        } catch (IOException e) {
            System.err.println("Error loading tasks: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Converts a list of tasks to JSON format.
     * 
     * @param tasks The tasks to convert
     * @return JSON string representation
     */
    private String tasksToJson(List<Task> tasks) {
        if (tasks == null) {
            return "[]";
        }
        
        StringBuilder json = new StringBuilder("[\n");
        boolean first = true;
        
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            
            // Skip null tasks
            if (task == null) {
                System.err.println("Warning: Skipping null task at index " + i);
                continue;
            }
            
            // Add comma before task (except for first task)
            if (!first) {
                json.append(",\n");
            }
            first = false;
            
            json.append("  ").append(taskToJson(task));
        }
        
        json.append("\n]");
        return json.toString();
    }

    /**
     * Converts a single task to JSON format.
     * 
     * @param task The task to convert
     * @return JSON string representation of the task
     */
    private String taskToJson(Task task) {
        // Null check to prevent NullPointerException
        if (task == null) {
            System.err.println("Warning: Attempted to convert null task to JSON");
            return "{}";
        }
        
        // Additional null checks for task fields
        String id = task.getId() != null ? task.getId() : "";
        String name = task.getName() != null ? task.getName() : "";
        String deadline = task.getDeadline() != null ? task.getDeadline().toString() : "";
        String type = task.getType() != null ? task.getType().name() : "WORK";
        String priority = task.getPriority() != null ? task.getPriority().name() : "MEDIUM";
        String repetition = task.getRepetition() != null ? task.getRepetition().name() : "NONE";
        String notes = task.getNotes() != null ? task.getNotes() : "";
        String completedAt = task.getCompletedAt() != null ? "\"" + task.getCompletedAt().toString() + "\"" : "null";
        
        return String.format(
            "{\"id\":\"%s\",\"name\":\"%s\",\"deadline\":\"%s\",\"type\":\"%s\",\"priority\":\"%s\",\"repetition\":\"%s\",\"notes\":\"%s\",\"completed\":%b,\"completedAt\":%s}",
            id,
            escapeJson(name),
            deadline,
            type,
            priority,
            repetition,
            escapeJson(notes),
            task.isCompleted(),
            completedAt
        );
    }

    /**
     * Parses JSON string to a list of tasks.
     * 
     * @param json The JSON string to parse
     * @return List of parsed tasks
     */
    private List<Task> jsonToTasks(String json) {
        List<Task> tasks = new ArrayList<>();
        
        if (json == null || json.trim().isEmpty()) {
            return tasks;
        }
        
        // Remove whitespace and outer brackets
        json = json.trim();
        if (json.startsWith("[")) {
            json = json.substring(1);
        }
        if (json.endsWith("]")) {
            json = json.substring(0, json.length() - 1);
        }
        
        // Split by objects (simple approach - assumes well-formed JSON)
        int braceCount = 0;
        int lastStart = 0;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (c == '{') {
                if (braceCount == 0) {
                    lastStart = i;
                }
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    String taskJson = json.substring(lastStart, i + 1);
                    Task task = jsonToTask(taskJson);
                    if (task != null) {
                        tasks.add(task);
                    }
                }
            }
        }
        
        return tasks;
    }

    /**
     * Parses a single task from JSON string.
     * 
     * @param json The JSON string representing a task
     * @return Parsed Task object, or null if parsing fails
     */
    private Task jsonToTask(String json) {
        try {
            String id = extractJsonValue(json, "id");
            String name = extractJsonValue(json, "name");
            String deadlineStr = extractJsonValue(json, "deadline");
            String typeStr = extractJsonValue(json, "type");
            String priorityStr = extractJsonValue(json, "priority");
            String repetitionStr = extractJsonValue(json, "repetition");
            String notes = extractJsonValue(json, "notes");
            String completedStr = extractJsonValue(json, "completed");
            
            // Validate required fields
            if (name == null || name.isEmpty()) {
                System.err.println("Warning: Task has no name, skipping");
                return null;
            }
            if (deadlineStr == null || deadlineStr.isEmpty()) {
                System.err.println("Warning: Task has no deadline, skipping");
                return null;
            }
            
            // Create task
            Task task = new Task(
                name,
                LocalDate.parse(deadlineStr),
                typeStr != null && !typeStr.isEmpty() ? TaskType.valueOf(typeStr) : TaskType.WORK,
                priorityStr != null && !priorityStr.isEmpty() ? Priority.valueOf(priorityStr) : Priority.MEDIUM,
                repetitionStr != null && !repetitionStr.isEmpty() ? RepetitionPattern.valueOf(repetitionStr) : RepetitionPattern.NONE
            );
            
            // Set ID (preserve original)
            if (id != null && !id.isEmpty()) {
                task.setId(id);
            }
            
            // Set notes
            if (notes != null && !notes.isEmpty()) {
                task.setNotes(notes);
            }
            
            // Set completed status
            if ("true".equals(completedStr)) {
                task.setCompleted(true);
            }
            
            return task;
        } catch (Exception e) {
            System.err.println("Error parsing task from JSON: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extracts a value for a given key from a JSON string.
     * This is a simple implementation that works for our use case.
     * 
     * @param json The JSON string
     * @param key The key to extract
     * @return The extracted value, or empty string if not found
     */
    private String extractJsonValue(String json, String key) {
        String searchFor = "\"" + key + "\":";
        int startIndex = json.indexOf(searchFor);
        
        if (startIndex == -1) {
            return "";
        }
        
        startIndex += searchFor.length();
        
        // Skip whitespace
        while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
            startIndex++;
        }
        
        // Check bounds
        if (startIndex >= json.length()) {
            return "";
        }
        
        // Check if value is a string (starts with ")
        if (json.charAt(startIndex) == '"') {
            startIndex++; // Skip opening quote
            int endIndex = startIndex;
            
            // Find closing quote (handle escaped quotes)
            while (endIndex < json.length()) {
                if (json.charAt(endIndex) == '"' && (endIndex == 0 || json.charAt(endIndex - 1) != '\\')) {
                    return unescapeJson(json.substring(startIndex, endIndex));
                }
                endIndex++;
            }
        } else {
            // Value is not a string (boolean, number, null)
            int endIndex = startIndex;
            while (endIndex < json.length() && 
                   json.charAt(endIndex) != ',' && 
                   json.charAt(endIndex) != '}') {
                endIndex++;
            }
            return json.substring(startIndex, endIndex).trim();
        }
        
        return "";
    }

    /**
     * Escapes special characters for JSON format.
     * 
     * @param str The string to escape
     * @return Escaped string
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * Unescapes special characters from JSON format.
     * 
     * @param str The string to unescape
     * @return Unescaped string
     */
    private String unescapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\\"", "\"")
                  .replace("\\\\", "\\")
                  .replace("\\n", "\n")
                  .replace("\\r", "\r")
                  .replace("\\t", "\t");
    }

    /**
     * Returns the path where tasks are stored.
     * 
     * @return Path to the tasks file
     */
    public Path getTasksFilePath() {
        return tasksFilePath;
    }
}