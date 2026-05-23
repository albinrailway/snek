package com.example.snek.controller;

import com.example.snek.util.Database;
import com.example.snek.util.SceneManager;
import com.example.snek.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        String hashedPw = hashPassword(password);
        String sql = "SELECT id, username FROM users WHERE username = ? AND password_hash = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hashedPw);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserSession.login(rs.getInt("id"), rs.getString("username"));
                SceneManager.switchScene("menu-view.fxml");
            } else {
                showError("Invalid credentials.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Database connection failed.");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Fields cannot be empty.");
            return;
        }

        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password));
            stmt.executeUpdate();

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Registration successful! You can now log in.");
            alert.showAndWait();
        } catch (Exception e) {
            showError("Username may already exist.");
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}