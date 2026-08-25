package com.example.newsfeedmanagementsystem.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ToastManager {

    private enum Type {SUCCESS, ERROR, INFO}

    private static Popup loadingPopup;

    public static void success(String message) {
        show(message, Type.SUCCESS);
    }

    public static void error(String message) {
        show(message, Type.ERROR);
    }

    public static void info(String message) {
        show(message, Type.INFO);
    }

    private static void show(String message, Type type) {
        Stage stage = SceneManager.getPrimaryStage();
        if (stage == null) return;

        Popup popup = new Popup();
        popup.setAutoFix(true);

        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(280);
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: 500;");

        HBox box = new HBox(label);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new javafx.geometry.Insets(12, 16, 12, 16));
        box.setStyle(
                "-fx-background-radius: 10;" +
                        "-fx-background-color: " + colorFor(type) + ";" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 20, 0.15, 0, 6);"
        );

        popup.getContent().add(box);

        popup.setOnShown(e -> {
            popup.setX(stage.getX() + stage.getWidth() - box.getWidth() - 24 - 20);
            popup.setY(stage.getY() + stage.getHeight() - box.getHeight() - 24 - 20);
        });

        popup.show(stage);

        box.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(180), box);
        fadeIn.setToValue(1);
        fadeIn.play();

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), box);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e2 -> popup.hide());
            fadeOut.play();
        });
        delay.play();
    }

    private static String colorFor(Type type) {
        return switch (type) {
            case SUCCESS -> "#2ea043"; // green
            case ERROR -> "#e5484d"; // red
            default -> "#4f9de8"; // blue
        };
    }

    public static void showLoading(String message) {
        Stage stage = SceneManager.getPrimaryStage();
        if (stage == null) return;

        hideLoading();

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(18, 18);
        spinner.setStyle("-fx-progress-color: white;");

        Label label = new Label(message);
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: 500;");

        HBox box = new HBox(10, spinner, label);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new javafx.geometry.Insets(12, 16, 12, 16));
        box.setStyle(
                "-fx-background-radius: 10;" +
                        "-fx-background-color: #262c3a;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 20, 0.15, 0, 6);"
        );

        loadingPopup = new Popup();
        loadingPopup.setAutoFix(true);
        loadingPopup.getContent().add(box);

        loadingPopup.setOnShown(e -> {
            loadingPopup.setX(stage.getX() + stage.getWidth() - box.getWidth() - 24 - 20);
            loadingPopup.setY(stage.getY() + stage.getHeight() - box.getHeight() - 24 - 20);
        });

        loadingPopup.show(stage);
    }

    public static void hideLoading() {
        if (loadingPopup != null) {
            loadingPopup.hide();
            loadingPopup = null;
        }
    }
}