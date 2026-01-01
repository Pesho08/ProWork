package com.prowork;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.concurrent.Worker;
import netscape.javascript.JSObject;
import java.net.URL;
import java.util.Optional;

/**
 * Main application class for ProWork task management system.
 * 
 * This JavaFX application creates a hybrid interface using WebView to display
 * HTML/CSS/JavaScript frontend with a Java backend. The application features:
 * - Task list view for managing tasks
 * - Calendar view for visualizing tasks by date
 * - Menu bar for navigation and help
 * - JavaScript-Java bridge for frontend-backend communication
 * 
 * The application uses a single WebView with different HTML pages loaded
 * dynamically. A static TaskManager ensures data persists across view changes.
 * 
 * @author Chris
 * @version 1.0
 */
public class App extends Application {
  private String appIconPath = "/assets/img/ProWork.png";
  private WebEngine engine;
  private JavaBridge bridge;

  /**
   * Starts the JavaFX application.
   * Sets up the menu bar, WebView, and loads the initial view.
   * 
   * @param stage The primary stage for this application
   */
  @Override
  public void start(Stage stage) {
    // Create MenuBar
    MenuBar menuBar = new MenuBar();
    
    // View Menu - allows switching between task list and calendar views
    Menu viewMenu = new Menu("Ansicht");
    MenuItem taskListItem = new MenuItem("Aufgabenliste");
    MenuItem calendarItem = new MenuItem("Kalenderansicht");
    
    taskListItem.setOnAction(e -> loadView("/index.html"));
    calendarItem.setOnAction(e -> loadView("/calendar.html"));
    
    viewMenu.getItems().addAll(taskListItem, calendarItem);
    
    // Help Menu
    Menu helpMenu = new Menu("Hilfe");
    MenuItem aboutItem = new MenuItem("Über ProWork");
    aboutItem.setOnAction(e -> showAbout());
    helpMenu.getItems().add(aboutItem);
    
    menuBar.getMenus().addAll(viewMenu, helpMenu);

    // Create WebView for hybrid HTML/JS frontend
    WebView view = new WebView();
    engine = view.getEngine();
    bridge = new JavaBridge(engine);
    
    // Enable JavaScript
    engine.setJavaScriptEnabled(true);
    
    // Setup Alert Handler - JavaFX WebView requires explicit alert handling
    // Without this, JavaScript alert() produces no visible output
    engine.setOnAlert(event -> {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("ProWork");
      alert.setHeaderText(null);
      alert.setContentText(event.getData());
      alert.showAndWait();
    });
    
    // Setup Confirm Handler - Required for JavaScript confirm() dialogs
    engine.setConfirmHandler(message -> {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("ProWork");
      alert.setHeaderText(null);
      alert.setContentText(message);
      Optional<ButtonType> result = alert.showAndWait();
      return result.isPresent() && result.get() == ButtonType.OK;
    });

    /**
     * Setup load listener to inject JavaBridge after page loads.
     * This ensures the javaBridge object is available to JavaScript code.
     * The bridge is re-injected each time a new page is loaded.
     */
    engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
      if (newState == Worker.State.SUCCEEDED) {
        injectBridge();
      }
    });

    // Load initial page (task list view)
    loadView("/index.html");

    // Create layout with menu bar and WebView
    BorderPane root = new BorderPane();
    root.setTop(menuBar);
    root.setCenter(view);

    // Set application icon if available
    try {
      stage.getIcons().add(new Image(getClass().getResourceAsStream(appIconPath)));
    } catch (Exception e) {
      System.out.println("App icon not found");
    }

    // Configure and show the stage
    stage.setTitle("ProWork");
    stage.setScene(new Scene(root, 1200, 800));
    stage.show();
  }

  /**
   * Loads an HTML view from the resources folder.
   * 
   * @param htmlFile The path to the HTML file (e.g., "/index.html")
   */
  private void loadView(String htmlFile) {
    URL url = getClass().getResource(htmlFile);
    if (url == null) {
      System.err.println("ERROR: " + htmlFile + " not found!");
      return;
    }
    
    System.out.println("Loading: " + htmlFile);
    engine.load(url.toExternalForm());
  }

  /**
   * Injects the JavaBridge object into the JavaScript context.
   * This allows JavaScript code to call Java methods via window.javaBridge.
   * 
   * Called automatically after each page load via the LoadWorker listener.
   */
  private void injectBridge() {
    try {
      JSObject window = (JSObject) engine.executeScript("window");
      window.setMember("javaBridge", bridge);
      System.out.println("JavaBridge injected successfully");
      
      // Verify injection succeeded
      Object test = engine.executeScript("typeof javaBridge");
      System.out.println("javaBridge type: " + test);
    } catch (Exception e) {
      System.err.println("Error injecting JavaBridge: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Displays the About dialog with application information.
   */
  private void showAbout() {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Über ProWork");
    alert.setHeaderText("ProWork v1.0");
    alert.setContentText("Ein Task-Manager mit Kalenderansicht\nErstellt mit JavaFX");
    alert.showAndWait();
  }

  /**
   * Main entry point for the application.
   * 
   * @param args Command line arguments (not used)
   */
  
  public static void main(String[] args) { 
    launch(); 
  }
}