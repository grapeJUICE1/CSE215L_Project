package com.example.newsfeedmanagementsystem.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {
    private static Stage primaryStage;
    private static Scene currentScene;

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static void switchTo(String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.class.getResource("/fxml/" + fxmlName + ".fxml")
            );
            Parent root = loader.load();

            if (currentScene == null) {
                currentScene = new Scene(root);
                ThemeManager.applyTheme(currentScene);
                primaryStage.setScene(currentScene);
                primaryStage.show();
            } else {
                currentScene.setRoot(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}