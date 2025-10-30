package com.snakegame;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.Canvas;
import javafx.application.Application;
import javafx.stage.Stage;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.input.KeyCode;
import javafx.animation.AnimationTimer;
import javafx.scene.text.Text;


public class SnakeApp extends Application {
    private static final int TILE_SIZE = 20;
    private static final int GRID_WIDTH = 30;
    private static final int GRID_HEIGHT = 20;
    private final long INITIAL_MOVE_DELAY = 200_000_000; // 200 milliseconds

    private Deque<Point> snake;
    private Direction direction;
    private Point food;
    private boolean gameOver = false;
    private int score = 0;
    private long moveDelay = INITIAL_MOVE_DELAY;
    private Random random = new Random();
    // current high-level game state (start screen, running, paused, game over)
    private enum GameState { MENU ,START, RUNNING, PAUSED, GAME_OVER }
    private GameState gameState = GameState.START;

    private void initializeGame() {
        snake = new LinkedList<>();
        int startx = GRID_WIDTH / 2;
        int starty = GRID_HEIGHT / 2;
        snake.addFirst(new Point(startx, starty));
        snake.addLast(new Point(startx - 1, starty));
        snake.addLast(new Point (startx - 2, starty));
    direction = Direction.RIGHT;
    placeFood();
    score = 0;
    moveDelay = INITIAL_MOVE_DELAY;
    gameOver = false;
    // when initialized via UI, we will set RUNNING; keep START until user starts
    gameState = GameState.RUNNING;

    }

    private void placeFood() {
        while(true){
            int fx = random.nextInt(GRID_WIDTH);
            int fy = random.nextInt(GRID_HEIGHT);
            Point candidate = new Point (fx, fy);
            boolean collides = false;
            for(Point p: snake){
                if(p.equals(candidate)){collides = true; break;}
            }
            if(!collides){
                food = candidate;
                return;}
        }
    }

    private void render(GraphicsContext gc) {
        // background
        gc.setFill(Color.web("#0b2b34"));
        gc.fillRect(0, 0, GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);

        // draw food if present
        if (food != null) {
            gc.setFill(Color.web("#ff5252"));
            gc.fillOval(food.x * TILE_SIZE + 2, food.y * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4);
        }

        // draw snake if present
        if (snake != null) {
            boolean head = true;
            for (Point p : snake) {
                if (head) {
                    gc.setFill(Color.web("#a6ffcb"));
                    gc.fillRect(p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    head = false;
                } else {
                    gc.setFill(Color.web("#50c890"));
                    gc.fillRect(p.x * TILE_SIZE + 2, p.y * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4);
                }
            }
        }

        // HUD (score)
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(18));
        gc.fillText("Score: " + score, 10, 20);

        double cx = (GRID_WIDTH * TILE_SIZE) / 2.0;
        double cy = (GRID_HEIGHT * TILE_SIZE) / 2.0;

        //Menu
        if (gameState == GameState.MENU){
            String title = "SNAKE GAME";
            String instr1 = "Press ENTER to start the game ";
            String instr2 = "S - Scoreboard";
            String instr3 = "Q - Quit";

            gc.setFill(Color.rgb(0, 0, 0, 0.6));
            gc.fillRect(0,0,GRID_WIDTH * GRID_HEIGHT * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(36));
            gc.fillText(title , cx -60 , cy -100);

            gc.setFont(Font.font(18));
            gc.fillText(instr1 , cx - 70 , cy );
            gc.fillText(instr2 , cx - 70 , cy - 30);
            gc.fillText(instr3 , cx -70 , cy - 60);

        }

        // overlays for different states
        if (gameState == GameState.START) {
            String title = "SNAKE";
            String instr = "Press ENTER to start";
            Font titleFont = Font.font(36);
            Font instrFont = Font.font(18);

            gc.setFill(Color.rgb(0, 0, 0, 0.6));
            gc.fillRect(0, 0, GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);

            Text t1 = new Text(title); t1.setFont(titleFont);
            Text t2 = new Text(instr); t2.setFont(instrFont);
            double w1 = t1.getLayoutBounds().getWidth();
            double w2 = t2.getLayoutBounds().getWidth();

            gc.setFill(Color.WHITE);
            gc.setFont(titleFont);
            gc.fillText(title, cx - w1 / 2.0, cy - 6);
            gc.setFont(instrFont);
            gc.fillText(instr, cx - w2 / 2.0, cy + 24);
        }

        if (gameState == GameState.PAUSED) {
            String title = "PAUSED";
            String instr = "Press SPACE to resume";
            Font titleFont = Font.font(36);
            Font instrFont = Font.font(18);

            gc.setFill(Color.rgb(0, 0, 0, 0.5));
            gc.fillRect(0, 0, GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);

            Text t1 = new Text(title); t1.setFont(titleFont);
            Text t2 = new Text(instr); t2.setFont(instrFont);
            double w1 = t1.getLayoutBounds().getWidth();
            double w2 = t2.getLayoutBounds().getWidth();

            gc.setFill(Color.WHITE);
            gc.setFont(titleFont);
            gc.fillText(title, cx - w1 / 2.0, cy - 6);
            gc.setFont(instrFont);
            gc.fillText(instr, cx - w2 / 2.0, cy + 24);
        }

        if (gameState == GameState.GAME_OVER || gameOver) {
            String title = "GAME OVER";
            String instr = "Press ENTER or R to restart";
            Font titleFont = Font.font(36);
            Font instrFont = Font.font(18);

            gc.setFill(Color.rgb(0, 0, 0, 0.6));
            gc.fillRect(0, 0, GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);

            Text t1 = new Text(title); t1.setFont(titleFont);
            Text t2 = new Text(instr); t2.setFont(instrFont);
            double w1 = t1.getLayoutBounds().getWidth();
            double w2 = t2.getLayoutBounds().getWidth();

            gc.setFill(Color.WHITE);
            gc.setFont(titleFont);
            gc.fillText(title, cx - w1 / 2.0, cy - 6);
            gc.setFont(instrFont);
            gc.fillText(instr, cx - w2 / 2.0, cy + 24);
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
    if (newHead.x < 0 || newHead.x >= GRID_WIDTH || newHead.y < 0 || newHead.y >= GRID_HEIGHT) {
        gameOver = true;
        gameState = GameState.GAME_OVER;
        return;
    }

    // collison with itself
    for (Point p : snake) {
        if (p.equals(newHead)) { gameOver = true; gameState = GameState.GAME_OVER; return; }
    }

    snake.addFirst(newHead);

    if (newHead.equals(food)) {
        score += 10;
        placeFood();
        if (score % 50 == 0 && moveDelay > 60_000_000L) moveDelay -= 20_000_000L;
    } else {
        snake.removeLast();
    }
}

    // class-level UI/game fields used by start() and the AnimationTimer
    private Canvas canvas;
    private GraphicsContext gc;
    private boolean movedSinceKey = true;
    private AnimationTimer timer;

    @Override
    public void start(Stage primaryStage){
    canvas = new Canvas(GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);
    StackPane root = new StackPane();
    root.getChildren().add(canvas);
    // set Scene size explicitly to match canvas so layout is stable
    Scene scene = new Scene(root, GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);
    gc = canvas.getGraphicsContext2D();
    // don't auto-start the game here; show start screen until user presses ENTER
    gameState = GameState.MENU;    
    primaryStage.setTitle("Snake Game");
    primaryStage.setScene(scene);
    primaryStage.show();
    // request focus so key events are received immediately
    canvas.requestFocus();

        scene.setOnKeyPressed(e -> {
            KeyCode code = e.getCode();

            if (gameState == GameState.MENU) {
                switch (code) {
                    case ENTER -> {
                        initializeGame();
                    }
                    case S -> {
                        // Show scoreboard - to be implemented
                    }
                    case Q -> {
                        primaryStage.close();
                    }
                    default -> {}
                }
            }

            // movement keys only have effect while running
            if (gameState == GameState.RUNNING) {
                switch (code) {
                    case UP, W -> { if (direction != Direction.DOWN && movedSinceKey) { direction = Direction.UP; movedSinceKey = false; } }
                    case DOWN, S -> { if (direction != Direction.UP && movedSinceKey) { direction = Direction.DOWN; movedSinceKey = false; } }
                    case LEFT, A -> { if (direction != Direction.RIGHT && movedSinceKey) { direction = Direction.LEFT; movedSinceKey = false; } }
                    case RIGHT, D -> { if (direction != Direction.LEFT && movedSinceKey) { direction = Direction.RIGHT; movedSinceKey = false; } }
                    default -> {}
                }
            }

            // Global controls
            if (code == KeyCode.ENTER) {
                if (gameState == GameState.START || gameState == GameState.GAME_OVER) {
                    initializeGame();
                } else if (gameState == GameState.PAUSED) {
                    gameState = GameState.RUNNING;
                }
            } else if (code == KeyCode.SPACE) {
                // toggle pause when running
                if (gameState == GameState.RUNNING) gameState = GameState.PAUSED;
                else if (gameState == GameState.PAUSED) gameState = GameState.RUNNING;
            } else if (code == KeyCode.R) {
                initializeGame();
            }
        });

        timer = new AnimationTimer() {
            private long lastMove = 0;
            @Override
            public void handle(long now) {
                // only advance game ticks when in RUNNING state
                if (now - lastMove >= moveDelay && gameState == GameState.RUNNING) {
                    update();
                    lastMove = now;
                    movedSinceKey = true;
                }
                // always render so overlays (start/pause/game over) appear
                render(gc);
            }
        };
        timer.start();
    }

private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
private static class Point {
        int x, y;
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        @Override public boolean equals(Object o){
            if (this == o) return true;
            if (!(o instanceof Point)) return false;
            Point p = (Point) o;
            return x == p.x && y == p.y;
        }
        @Override public int hashCode(){
            return x * 31 + y;
        }
}

    public static void main(String[] args) {
        launch(args);
    }
    
}
