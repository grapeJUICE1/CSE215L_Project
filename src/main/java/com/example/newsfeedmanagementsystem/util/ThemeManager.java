package com.example.newsfeedmanagementsystem.util;

import javafx.scene.Scene;

public class ThemeManager {
    private static boolean darkMode = true;

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void applyTheme(Scene scene) {
        if (scene == null) return;
        scene.getStylesheets().clear();
        String cssPath = darkMode ? "/css/dark-theme.css" : "/css/light-theme.css";
        scene.getStylesheets().add(ThemeManager.class.getResource(cssPath).toExternalForm());
    }

    public static void toggleTheme(Scene scene) {
        darkMode = !darkMode;
        applyTheme(scene);
    }
}
