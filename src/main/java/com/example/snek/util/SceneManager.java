package com.example.snek.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneManager {
    private static Stage primaryStage;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchScene(String fxmlFile) {
        try {
            // FIXED: Pointing directly to the subfolder your FXML files are in
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/com/example/snek/" + fxmlFile));
            primaryStage.setScene(new Scene(loader.load(), 800, 600));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load scene: " + fxmlFile);
        }
    }
}