package com.example.newsfeedmanagementsystem.model;

import java.util.Set;

public class Journalist extends User {
    public Journalist(String username, String passwordHash, String displayName) {
        super(username, passwordHash, displayName);
    }

    public Journalist(String username, String passwordHash, String displayName, Set<String> bookmarks) {
        super(username, passwordHash, displayName, bookmarks);
    }

    @Override
    public boolean canPublish() {
        return true;
    }
}
