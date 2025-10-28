package com.snakegame;

import javafx.application.Application;
import javafx.stage.Stage;

public class SnakeApp extends Application {
    @Override
    public void start(Stage primaryStage){

        primaryStage.setTitle("Snake Game");
        primaryStage.show();

    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
