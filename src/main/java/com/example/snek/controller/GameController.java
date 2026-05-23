package com.example.snek.controller;

import com.example.snek.util.Database;
import com.example.snek.util.SceneManager;
import com.example.snek.util.UserSession;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button; // FIXED: Added missing import
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameController {
    @FXML
    private VBox rootBox;
    @FXML
    private Canvas gameCanvas;
    @FXML
    private Label scoreLabel;
    @FXML
    private Button pauseButton;

    // FIXED: Removed duplicate declaration of gameOver from this block
    private boolean isPaused = false;
    private GraphicsContext gc;

    private static final int TILE_SIZE = 20;
    private static final int WIDTH = 40;
    private static final int HEIGHT = 25;

    private List<Point> snake = new ArrayList<>();
    private Point food;
    private Direction direction = Direction.RIGHT;
    private boolean gameOver = false;
    private int score = 0;
    private AnimationTimer loop;

    private enum Direction {UP, DOWN, LEFT, RIGHT}

    private record Point(int x, int y) {}

    @FXML
    private void handlePause() {
        togglePause();
        rootBox.requestFocus();
    }

    @FXML
    private void handleQuit() {
        loop.stop();
        SceneManager.switchScene("menu-view.fxml");
    }

    private void togglePause() {
        if (gameOver) return;

        isPaused = !isPaused;
        if (isPaused) {
            loop.stop();
            if (pauseButton != null) pauseButton.setText("resume");
        } else {
            loop.start();
            if (pauseButton != null) pauseButton.setText("pause");
        }
        draw();
    }

    @FXML
    public void initialize() {
        gc = gameCanvas.getGraphicsContext2D();

        snake.add(new Point(WIDTH / 2, HEIGHT / 2));
        spawnFood();

        Platform.runLater(() -> {
            rootBox.getScene().setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.P) {
                    togglePause();
                }

                if (!isPaused) {
                    if (event.getCode() == KeyCode.UP && direction != Direction.DOWN) direction = Direction.UP;
                    if (event.getCode() == KeyCode.DOWN && direction != Direction.UP) direction = Direction.DOWN;
                    if (event.getCode() == KeyCode.LEFT && direction != Direction.RIGHT) direction = Direction.LEFT;
                    if (event.getCode() == KeyCode.RIGHT && direction != Direction.LEFT) direction = Direction.RIGHT;
                }
            });
            rootBox.requestFocus();
        });

        loop = new AnimationTimer() {
            long lastTick = 0;

            public void handle(long now) {
                if (lastTick == 0 || now - lastTick > 100_000_000) {
                    tick();
                    lastTick = now;
                }
            }
        };
        loop.start();
    }

    private void tick() {
        if (gameOver) {
            loop.stop();
            saveScoreToSupabase(UserSession.getUserId(), score);
            Platform.runLater(() -> SceneManager.switchScene("highscore-view.fxml"));
            return;
        }

        Point head = snake.get(0);
        // FIXED: Using accessor methods () for record types
        int nextX = head.x();
        int nextY = head.y();

        switch (direction) {
            case UP -> nextY--;
            case DOWN -> nextY++;
            case LEFT -> nextX--;
            case RIGHT -> nextX++;
        }

        if (nextX < 0 || nextX >= WIDTH || nextY < 0 || nextY >= HEIGHT || isSnake(nextX, nextY)) {
            gameOver = true;
            return;
        }

        snake.add(0, new Point(nextX, nextY));

        // FIXED: Using accessor methods () for record types
        if (nextX == food.x() && nextY == food.y()) {
            score += 10;
            scoreLabel.setText("score: " + score);
            spawnFood();
        } else {
            snake.remove(snake.size() - 1);
        }

        draw();
    }

    private void draw() {
        gc.setFill(Color.web("#FAF5ED"));
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        gc.setFill(Color.web("#FFB6B9"));
        gc.fillOval(food.x() * TILE_SIZE, food.y() * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            if (i == 0) {
                gc.setFill(Color.web("#A0C490"));
            } else {
                gc.setFill(Color.web("#C5E0B4"));
            }
            gc.fillOval(p.x() * TILE_SIZE, p.y() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        if (isPaused) {
            gc.setFill(Color.web("#FAF5ED", 0.7));
            gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

            gc.setFill(Color.web("#7D9B76"));
            gc.setFont(javafx.scene.text.Font.font("Comic Sans MS", javafx.scene.text.FontWeight.BOLD, 40));
            gc.fillText("paused", gameCanvas.getWidth() / 2 - 70, gameCanvas.getHeight() / 2);
        }
    }

    private void spawnFood() {
        Random rand = new Random();
        int x, y;
        do {
            x = rand.nextInt(WIDTH);
            y = rand.nextInt(HEIGHT);
        } while (isSnake(x, y));
        food = new Point(x, y);
    }

    private boolean isSnake(int x, int y) {
        // FIXED: Using accessor methods () for record types
        return snake.stream().anyMatch(p -> p.x() == x && p.y() == y);
    }

    private void saveScoreToSupabase(int userId, int score) {
        if (userId == -1 || score == 0) return;

        String sql = "INSERT INTO highscores (user_id, score) VALUES (?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, score);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}