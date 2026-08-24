package com.example.newsfeedmanagementsystem.model;

import java.util.Set;

public class RegularUser extends User {
    public RegularUser(String username, String passwordHash, String displayName) {
        super(username, passwordHash, displayName);
    }

    public RegularUser(String username, String passwordHash, String displayName, Set<String> bookmarks) {
        super(username, passwordHash, displayName, bookmarks);
    }

    @Override
    public boolean canPublish() {
        return false;
    }
}
