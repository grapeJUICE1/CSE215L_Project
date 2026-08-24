package com.example.newsfeedmanagementsystem.util;

import com.example.newsfeedmanagementsystem.model.Article;
import com.example.newsfeedmanagementsystem.model.User;

public class AppState {
    private static Article selectedArticle;
    private static User viewedProfileUser;

    public static void setSelectedArticle(Article selectedArticle) {
        AppState.selectedArticle = selectedArticle;
    }

    public static void setViewedProfileUser(User viewedProfileUser) {
        AppState.viewedProfileUser = viewedProfileUser;
    }

    public static Article getSelectedArticle() {
        return selectedArticle;
    }

    public static User getViewedProfileUser() {
        return viewedProfileUser;
    }

}
