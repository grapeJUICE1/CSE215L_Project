package com.example.newsfeedmanagementsystem.model;

import java.io.Serializable;

public abstract class User implements Serializable {
    private String username;
    private String passwordHash;
    private String displayName;
    private boolean isBanned = false;
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

    public boolean isBanned() {
       return isBanned;
    }

    public void setBanned(boolean banned) {
        isBanned = banned;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof User)) return false;
        User other =  (User) o;
        return username.equals(other.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s (@%s) [%s]", displayName, username, getClass().getSimpleName());
    }
}
