package com.example.newsfeedmanagementsystem;

import com.example.newsfeedmanagementsystem.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage primaryStage) {
        SceneManager.init(primaryStage);
        primaryStage.setTitle("News Feed Management System");
        SceneManager.switchTo("login");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
