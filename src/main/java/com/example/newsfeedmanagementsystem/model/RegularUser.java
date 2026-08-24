package com.example.newsfeedmanagementsystem.model;

public class RegularUser extends User {
    public RegularUser(String username, String passwordHash, String displayName) {
        super(username, passwordHash, displayName);
    }

    @Override
    public boolean canPublish() {
        return false;
    }
}
