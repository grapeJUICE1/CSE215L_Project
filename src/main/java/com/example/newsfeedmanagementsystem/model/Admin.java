package com.example.newsfeedmanagementsystem.model;

public class Admin extends User{
    public Admin(String username, String passwordHash, String displayName) {
        super(username, passwordHash, displayName);
    }
    @Override
    public boolean canPublish() {
        return true;
    }
}
