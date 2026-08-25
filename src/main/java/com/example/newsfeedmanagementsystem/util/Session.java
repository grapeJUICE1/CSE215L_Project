package com.example.newsfeedmanagementsystem.util;

import com.example.newsfeedmanagementsystem.model.User;

public class Session {
    private static User currentUser;

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

}
