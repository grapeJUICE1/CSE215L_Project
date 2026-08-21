package com.example.newsfeedmanagementsystem.model;

import java.io.Serializable;

public abstract class User implements Serializable {
    private String username;
    private String passwordHash;
    private String displayName;
    private static final long serialVersionUID = 1L;

    User(String username, String passwordHash, String displayName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }
    public abstract boolean canPublish();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return String.format("%s (@%s) [%s]", displayName, username, getClass().getSimpleName());
    }
}
