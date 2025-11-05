package com.snakegame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.input.KeyCode;
import javafx.animation.AnimationTimer;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class SnakeApp extends Application {
    // Internal classes first
    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
    
    private static class Point {
        int x, y;
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        @Override 
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Point)) return false;
            Point p = (Point) o;
            return x == p.x && y == p.y;
        }
        @Override 
        public int hashCode() {
            return x * 31 + y;
        }
    }

    // Game constants
    private static final int TILE_SIZE = 20;
    private static final int GRID_WIDTH = 30;
    private static final int GRID_HEIGHT = 20;
    private final long INITIAL_MOVE_DELAY = 200_000_000; // 200 milliseconds

    // Game state
    private Deque<Point> snake;
    private Direction direction;
    private Point food;
    private boolean gameOver = false;
    private int score = 0;
    private long moveDelay = INITIAL_MOVE_DELAY;
    private Random random = new Random();
    private StringBuilder nameInput = new StringBuilder();
    
    // UI state
    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer timer;
    private boolean movedSinceKey = true;

    // Game state enum and current state
    private enum GameState { 
        MENU, START, RUNNING, PAUSED, GAME_OVER, Scores, Name_Entry
    }
    private GameState gameState = GameState.START;

    private void initializeGame() {
        snake = new LinkedList<>();
        int startx = GRID_WIDTH / 2;
        int starty = GRID_HEIGHT / 2;
        snake.addFirst(new Point(startx, starty));
        snake.addLast(new Point(startx - 1, starty));
        snake.addLast(new Point(startx - 2, starty));
        direction = Direction.RIGHT;
        placeFood();
        score = 0;
        moveDelay = INITIAL_MOVE_DELAY;
        gameOver = false;
        gameState = GameState.RUNNING;
    }

    private void placeFood() {
        while (true) {
            int fx = random.nextInt(GRID_WIDTH);
            int fy = random.nextInt(GRID_HEIGHT);
            Point candidate = new Point(fx, fy);
            boolean collides = false;
            for (Point p : snake) {
                if (p.equals(candidate)) {
                    collides = true;
                    break;
                }
            }
            if (!collides) {
                food = candidate;
                return;
            }
        }
    }

    // Score handling
    private java.util.List<String> loadScores() {
        java.util.List<String> lines = new java.util.ArrayList<>();
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.FileReader("scores.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            lines.add("No scores available.");
        }
        return lines;
    }
    
    private void saveScoreWithName(int score, String name) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        try (java.io.BufferedReader br = new java.io.BufferedReader(
            new java.io.FileReader("scores.txt"))){
                String line ;
                while ((line = br.readLine()) != null){
                    lines.add(line);

                }
            } catch (Exception e) {
    }
    lines.add(name + " " + score);

    lines.sort((a,b)-> {
        try {
            int scoreA= Integer.parseInt(a.replaceAll("\\D",""));
            int scoreB= Integer.parseInt(b.replaceAll("\\D",""));
            return Integer.compare(scoreB,scoreA);
        }catch(Exception ex){
            return 0;
        }
    });
    try(java.io.BufferedWriter bw =new java.io.BufferedWriter(
        new java.io.FileWriter("scores.txt", false ))){
            for(String s : lines){
                bw.write(s);
                bw.newLine();
            }
        }catch (Exception e){
            System.err.println("Error saving score: " + e.getMessage());
        }
    }

    private void update() {
        Point head = snake.peekFirst();
        Point newHead = switch (direction) {
            case UP -> new Point(head.x, head.y - 1);
            case DOWN -> new Point(head.x, head.y + 1);
            case LEFT -> new Point(head.x - 1, head.y);
            case RIGHT -> new Point(head.x + 1, head.y);
        };

        // collision with walls
        if (newHead.x < 0 || newHead.x >= GRID_WIDTH || 
            newHead.y < 0 || newHead.y >= GRID_HEIGHT) {
            gameOver = true;
            gameState = GameState.GAME_OVER;
            return;
        }

        // collision with self
        for (Point p : snake) {
            if (p.equals(newHead)) {
                gameOver = true;
                gameState = GameState.GAME_OVER;
                return;
            }
        }

        snake.addFirst(newHead);

        if (newHead.equals(food)) {
            score += 10;
            placeFood();
            if (score % 50 == 0 && moveDelay > 60_000_000L) {
                moveDelay -= 20_000_000L;
            }
        } else {
            snake.removeLast();
        }
    }

    private void render(GraphicsContext gc) {
        // background
        gc.setFill(Color.web("#0b2b34"));
        gc.fillRect(0, 0, GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);

        // draw food
        if (food != null) {
            gc.setFill(Color.web("#ff5252"));
            gc.fillOval(food.x * TILE_SIZE + 2, food.y * TILE_SIZE + 2, 
                       TILE_SIZE - 4, TILE_SIZE - 4);
        }

        // draw snake
        if (snake != null) {
            boolean isHead = true;
            for (Point p : snake) {
                if (isHead) {
                    gc.setFill(Color.web("#a6ffcb"));
                    gc.fillRect(p.x * TILE_SIZE, p.y * TILE_SIZE, 
                              TILE_SIZE, TILE_SIZE);
                    isHead = false;
                } else {
                    gc.setFill(Color.web("#50c890"));
                    gc.fillRect(p.x * TILE_SIZE + 2, p.y * TILE_SIZE + 2,
                              TILE_SIZE - 4, TILE_SIZE - 4);
                }
            }
        }

        // score display
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(18));
        gc.fillText("Score: " + score, 10, 20);

        // center coordinates for overlays
        double cx = (GRID_WIDTH * TILE_SIZE) / 2.0;
        double cy = (GRID_HEIGHT * TILE_SIZE) / 2.0;

        // render appropriate overlay based on game state
        switch (gameState) {
            case MENU -> {
                String title = "SNAKE GAME";
                String instr1 = "Press ENTER to start";
                String instr2 = "S - Scoreboard";
                String instr3 = "Q - Quit";

                gc.setFill(Color.rgb(0, 0, 0, 0.6));
                gc.fillRect(0, 0, GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);

                gc.setFill(Color.WHITE);
                gc.setFont(Font.font(36));
                gc.fillText(title, cx - 100, cy - 100);

                gc.setFont(Font.font(18));
                gc.fillText(instr1, cx - 70, cy);
                gc.fillText(instr2, cx - 70, cy + 30);
                gc.fillText(instr3, cx - 70, cy + 60);
            }
            case Scores -> {
                java.util.List<String> scores = loadScores();
                
                gc.setFill(Color.rgb(0, 0, 0, 0.6));
                gc.fillRect(0, 0, GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);

                gc.setFill(Color.WHITE);
                gc.setFont(Font.font(24));
                gc.fillText("Scoreboard", cx - 60, cy - 100);

                gc.setFont(Font.font(18));
                int offset = 0;
                for (String score : scores) {
                    gc.fillText(score, cx - 50, cy - 70 + offset);
                    offset += 20;
                }
                gc.fillText("Press ENTER to return", cx - 80, cy + 100);
            }
            case START -> {
                String title = "SNAKE";
                String instr = "Press ENTER to start";

                gc.setFill(Color.rgb(0, 0, 0, 0.6));
                gc.fillRect(0, 0, GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);

                gc.setFont(Font.font(36));
                Text t1 = new Text(title);
                t1.setFont(Font.font(36));
                double w1 = t1.getLayoutBounds().getWidth();

                gc.setFill(Color.WHITE);
                gc.fillText(title, cx - w1/2, cy - 20);
                
                gc.setFont(Font.font(18));
                Text t2 = new Text(instr);
                t2.setFont(Font.font(18));
                double w2 = t2.getLayoutBounds().getWidth();
                gc.fillText(instr, cx - w2/2, cy + 20);
            }
            case PAUSED -> {
                String title = "PAUSED";
                String instr = "Press SPACE to resume";

                gc.setFill(Color.rgb(0, 0, 0, 0.5));
                gc.fillRect(0, 0, GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);

                gc.setFont(Font.font(36));
                Text t1 = new Text(title);
                t1.setFont(Font.font(36));
                double w1 = t1.getLayoutBounds().getWidth();

                gc.setFill(Color.WHITE);
                gc.fillText(title, cx - w1/2, cy - 20);

                gc.setFont(Font.font(18));
                Text t2 = new Text(instr);
                t2.setFont(Font.font(18));
                double w2 = t2.getLayoutBounds().getWidth();
                gc.fillText(instr, cx - w2/2, cy + 20);
            }
            case GAME_OVER -> {
                String title = "GAME OVER";
                String instr = "N - Save Score, ENTER - Menu, R - Restart";

                gc.setFill(Color.rgb(0, 0, 0, 0.6));
                gc.fillRect(0, 0, GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);

                gc.setFont(Font.font(36));
                Text t1 = new Text(title);
                t1.setFont(Font.font(36));
                double w1 = t1.getLayoutBounds().getWidth();

                gc.setFill(Color.WHITE);
                gc.fillText(title, cx - w1/2, cy - 20);

                gc.setFont(Font.font(18));
                Text t2 = new Text(instr);
                t2.setFont(Font.font(18));
                double w2 = t2.getLayoutBounds().getWidth();
                gc.fillText(instr, cx - w2/2, cy + 20);
            }
            case Name_Entry -> {
                String title = "ENTER YOUR NAME";
                String instr = "Type name and press ENTER";
                String current = nameInput.length() > 0 ? nameInput.toString() : "_";

                gc.setFill(Color.rgb(0, 0, 0, 0.6));
                gc.fillRect(0, 0, GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);

                gc.setFont(Font.font(36));
                Text t1 = new Text(title);
                t1.setFont(Font.font(36));
                double w1 = t1.getLayoutBounds().getWidth();

                gc.setFill(Color.WHITE);
                gc.fillText(title, cx - w1/2, cy - 40);

                gc.setFont(Font.font(18));
                Text t2 = new Text(instr);
                t2.setFont(Font.font(18));
                double w2 = t2.getLayoutBounds().getWidth();
                gc.fillText(instr, cx - w2/2, cy);

                Text t3 = new Text(current);
                t3.setFont(Font.font(24));
                double w3 = t3.getLayoutBounds().getWidth();
                gc.setFont(Font.font(24));
                gc.fillText(current, cx - w3/2, cy + 40);
            }
            default -> { }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        canvas = new Canvas(GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);
        gc = canvas.getGraphicsContext2D();
        
        StackPane root = new StackPane();
        root.getChildren().add(canvas);
        
        Scene scene = new Scene(root, GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);
        
        primaryStage.setTitle("Snake Game");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        canvas.requestFocus();
        
        scene.setOnKeyPressed(e -> {
            KeyCode code = e.getCode();

            // Handle name entry first
            if (gameState == GameState.Name_Entry) {
                if (code == KeyCode.ENTER) {
                    String name = nameInput.length() == 0 ? "Player" : nameInput.toString();
                    saveScoreWithName(score, name);
                    nameInput.setLength(0);
                    gameState = GameState.Scores;
                    return;
                } else if (code == KeyCode.BACK_SPACE && nameInput.length() > 0) {
                    nameInput.deleteCharAt(nameInput.length() - 1);
                    return;
                } else {
                    String txt = e.getText();
                    if (txt != null && txt.length() > 0) {
                        char ch = txt.charAt(0);
                        if ((Character.isLetterOrDigit(ch) || Character.isSpaceChar(ch)) 
                            && nameInput.length() < 15) {
                            nameInput.append(ch);
                        }
                    }
                    return;
                }
            }

            // Handle menu navigation
            if (gameState == GameState.MENU) {
                if (code == KeyCode.ENTER) {
                    gameState = GameState.START;
                } else if (code == KeyCode.S) {
                    gameState = GameState.Scores;
                } else if (code == KeyCode.Q) {
                    primaryStage.close();
                }
                return;
            }

            // Handle score display
            if (gameState == GameState.Scores) {
                if (code == KeyCode.ENTER) {
                    gameState = GameState.MENU;
                }
                return;
            }

            // Global controls
            switch (code) {
                case ENTER -> {
                    if (gameState == GameState.START) {
                        initializeGame();
                    } else if (gameState == GameState.GAME_OVER) {
                        gameState = GameState.MENU;
                    }
                }
                case SPACE -> {
                    if (gameState == GameState.RUNNING) {
                        gameState = GameState.PAUSED;
                    } else if (gameState == GameState.PAUSED) {
                        gameState = GameState.RUNNING;
                    }
                }
                case R -> {
                    if (gameState == GameState.GAME_OVER || gameState == GameState.RUNNING) {
                        initializeGame();
                    }
                }
                case N -> {
                    if (gameState == GameState.GAME_OVER) {
                        gameState = GameState.Name_Entry;
                    }
                }
                case ESCAPE -> {
                    if (gameState == GameState.RUNNING) {
                        gameState = GameState.MENU;
                    }
                }
                default -> {
                    // Handle movement only when running
                    if (gameState == GameState.RUNNING) {
                        switch (code) {
                            case UP, W -> {
                                if (direction != Direction.DOWN && movedSinceKey) {
                                    direction = Direction.UP;
                                    movedSinceKey = false;
                                }
                            }
                            case DOWN, S -> {
                                if (direction != Direction.UP && movedSinceKey) {
                                    direction = Direction.DOWN;
                                    movedSinceKey = false;
                                }
                            }
                            case LEFT, A -> {
                                if (direction != Direction.RIGHT && movedSinceKey) {
                                    direction = Direction.LEFT;
                                    movedSinceKey = false;
                                }
                            }
                            case RIGHT, D -> {
                                if (direction != Direction.LEFT && movedSinceKey) {
                                    direction = Direction.RIGHT;
                                    movedSinceKey = false;
                                }
                            }
                            default -> {}
                        }
                    }
                }
            }
        });

        // Start game loop
        timer = new AnimationTimer() {
            private long lastMove = 0;
            
            @Override
            public void handle(long now) {
                if (now - lastMove >= moveDelay && gameState == GameState.RUNNING) {
                    update();
                    lastMove = now;
                    movedSinceKey = true;
                }
                render(gc);
            }
        };
        timer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}