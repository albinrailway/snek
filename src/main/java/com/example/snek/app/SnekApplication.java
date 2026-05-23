package com.example.snek.app;

import com.example.snek.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class SnekApplication extends Application {
    @Override
    public void start(Stage stage) {
        SceneManager.setStage(stage);
        stage.setTitle("Snek Terminal");
        stage.setResizable(false); // Keep game resolution locked
        SceneManager.switchScene("login-view.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }
}