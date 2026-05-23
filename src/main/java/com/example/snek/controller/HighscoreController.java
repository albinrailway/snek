package com.example.snek.controller;

import com.example.snek.util.Database;
import com.example.snek.util.SceneManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HighscoreController {
    @FXML private TableView<ScoreRecord> scoreTable;
    @FXML private TableColumn<ScoreRecord, String> playerColumn;
    @FXML private TableColumn<ScoreRecord, Integer> scoreColumn;

    public record ScoreRecord(String player, int score) {}

    @FXML
    public void initialize() {
        // Explicitly map the columns to the record fields
        playerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().player()));
        scoreColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().score()).asObject());

        loadScores();
    }

    private void loadScores() {
        ObservableList<ScoreRecord> scores = FXCollections.observableArrayList();
        String sql = "SELECT u.username, h.score FROM highscores h JOIN users u ON h.user_id = u.id ORDER BY h.score DESC LIMIT 10";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                scores.add(new ScoreRecord(rs.getString("username"), rs.getInt("score")));
            }

            if (scores.isEmpty()) {
                System.out.println("Query executed, but no records found in database.");
            }

            scoreTable.setItems(scores);
        } catch (Exception e) {
            e.printStackTrace();
            // Show alert so you know exactly why it fails
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setContentText("Failed to load scores: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneManager.switchScene("menu-view.fxml");
    }
}