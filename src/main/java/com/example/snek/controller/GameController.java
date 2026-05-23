package com.example.snek.controller;

import com.example.snek.util.Database;
import com.example.snek.util.SceneManager;
import com.example.snek.util.UserSession;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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

    // 1. Promoted to class level to avoid lambda closure strictness
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

    private record Point(int x, int y) {
    }

    @FXML
    public void initialize() {
        // 2. Initialize it safely here
        gc = gameCanvas.getGraphicsContext2D();

        snake.add(new Point(WIDTH / 2, HEIGHT / 2));
        spawnFood();

        Platform.runLater(() -> {
            rootBox.getScene().setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.UP && direction != Direction.DOWN) direction = Direction.UP;
                if (event.getCode() == KeyCode.DOWN && direction != Direction.UP) direction = Direction.DOWN;
                if (event.getCode() == KeyCode.LEFT && direction != Direction.RIGHT) direction = Direction.LEFT;
                if (event.getCode() == KeyCode.RIGHT && direction != Direction.LEFT) direction = Direction.RIGHT;
            });
            rootBox.requestFocus();
        });

        loop = new AnimationTimer() {
            long lastTick = 0;

            public void handle(long now) {
                if (lastTick == 0 || now - lastTick > 100_000_000) {
                    tick(); // 3. No longer passing gc as a parameter
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
        int nextX = head.x;
        int nextY = head.y;

        switch (direction) {
            case UP -> nextY--;
            case DOWN -> nextY++;
            case LEFT -> nextX--;
            case RIGHT -> nextX++;
        }

        // Checking collisions using the helper method to avoid lambda scope errors
        if (nextX < 0 || nextX >= WIDTH || nextY < 0 || nextY >= HEIGHT || isSnake(nextX, nextY)) {
            gameOver = true;
            return;
        }

        snake.add(0, new Point(nextX, nextY));

        if (nextX == food.x && nextY == food.y) {
            score += 10;
            scoreLabel.setText("Score: " + score);
            spawnFood();
        } else {
            snake.remove(snake.size() - 1);
        }

        draw();
    }

    private void draw() {
        gc.setFill(Color.web("#1E1E1E"));
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        gc.setFill(Color.web("#D32F2F"));
        gc.fillOval(food.x * TILE_SIZE, food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        gc.setFill(Color.web("#1976D2"));
        for (Point p : snake) {
            gc.fillRect(p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE - 1, TILE_SIZE - 1);
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
        return snake.stream().anyMatch(p -> p.x == x && p.y == y);
    }

    private void saveScoreToSupabase(int userId, int score) {
        if (userId == -1 || score == 0) return;

        String sql = "INSERT INTO highscores (user_id, score) VALUES (?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, score); // This is the line that was cut off!
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}