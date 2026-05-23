package com.example.snek.controller;

import com.example.snek.util.SceneManager;
import com.example.snek.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class MenuController {

    @FXML
    private void handlePlay(ActionEvent event) {
        SceneManager.switchScene("game-view.fxml");
    }

    @FXML
    private void handleHighscores(ActionEvent event) {
        SceneManager.switchScene("highscore-view.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        UserSession.logout();
        SceneManager.switchScene("login-view.fxml");
    }
}