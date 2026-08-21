package com.example.newsfeedmanagementsystem.model;

public class Journalist extends User{
    public Journalist(String username, String passwordHash, String displayName) {
        super(username, passwordHash, displayName);
    }
    @Override
    public boolean canPublish() {
        return true;
    }
}
