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

public class App extends Application {
  private String appIconPath = "/assets/img/ProWork.png";
  private WebEngine engine;
  private JavaBridge bridge;

  @Override
  public void start(Stage stage) {
    // Create MenuBar
    MenuBar menuBar = new MenuBar();
    
    // View Menu
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

    // Create WebView
    WebView view = new WebView();
    engine = view.getEngine();
    bridge = new JavaBridge(engine);
    
    // Enable JavaScript
    engine.setJavaScriptEnabled(true);
    
    // Setup Alert Handler
    engine.setOnAlert(event -> {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("ProWork");
      alert.setHeaderText(null);
      alert.setContentText(event.getData());
      alert.showAndWait();
    });
    
    // Setup Confirm Handler
    engine.setConfirmHandler(message -> {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("ProWork");
      alert.setHeaderText(null);
      alert.setContentText(message);
      Optional<ButtonType> result = alert.showAndWait();
      return result.isPresent() && result.get() == ButtonType.OK;
    });

    // Setup ONE listener that handles all page loads
    engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
      if (newState == Worker.State.SUCCEEDED) {
        injectBridge();
      }
    });

    // Load initial page
    loadView("/index.html");

    // Create layout
    BorderPane root = new BorderPane();
    root.setTop(menuBar);
    root.setCenter(view);

    // Set app icon
    try {
      stage.getIcons().add(new Image(getClass().getResourceAsStream(appIconPath)));
    } catch (Exception e) {
      System.out.println("App icon not found");
    }

    stage.setTitle("ProWork");
    stage.setScene(new Scene(root, 1200, 800));
    stage.show();
  }

  private void loadView(String htmlFile) {
    URL url = getClass().getResource(htmlFile);
    if (url == null) {
      System.err.println("ERROR: " + htmlFile + " not found!");
      return;
    }
    
    System.out.println("Loading: " + htmlFile);
    engine.load(url.toExternalForm());
  }

  private void injectBridge() {
    try {
      JSObject window = (JSObject) engine.executeScript("window");
      window.setMember("javaBridge", bridge);
      System.out.println("JavaBridge injected successfully");
      
      // Test if it's accessible
      Object test = engine.executeScript("typeof javaBridge");
      System.out.println("javaBridge type: " + test);
    } catch (Exception e) {
      System.err.println("Error injecting JavaBridge: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void showAbout() {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Über ProWork");
    alert.setHeaderText("ProWork v1.0");
    alert.setContentText("Ein Task-Manager mit Kalenderansicht\nErstellt mit JavaFX");
    alert.showAndWait();
  }

  public static void main(String[] args) { 
    launch(); 
  }
}