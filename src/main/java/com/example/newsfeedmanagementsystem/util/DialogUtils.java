package com.example.newsfeedmanagementsystem.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class DialogUtils {
    public static boolean confirm(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}