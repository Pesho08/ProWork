package com.prowork;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class App extends Application {
  private String appIconPath = "/assets/img/ProWork.png";

  @Override
  public void start(Stage stage) {
    WebView view = new WebView();
    var engine = view.getEngine();
    JavaBridge bridge = new JavaBridge(engine);

    engine.load(getClass().getResource("/index.html").toExternalForm());
    engine.getLoadWorker().stateProperty().addListener((o, old, state) -> {
      if (state == javafx.concurrent.Worker.State.SUCCEEDED) {
        // EXPOSE THE JAVA BRIDGE OBJECT DIRECTLY TO JavaScript
        var window = (netscape.javascript.JSObject) engine.executeScript("window");
        window.setMember("javaApp", bridge);
        String location = engine.getLocation();
      }
    });

    // SET APP ICON
    try {
      stage.getIcons().add(new Image(getClass().getResourceAsStream(appIconPath)));
    } catch (Exception e) {
      System.out.println("App icon '" + appIconPath + "'not found: " + e.getMessage());
    }

    stage.setTitle("ProWork");
    stage.setScene(new Scene(view, 1200, 800));
    stage.show();
  }

  public static void main(String[] args) { launch(); }
}
