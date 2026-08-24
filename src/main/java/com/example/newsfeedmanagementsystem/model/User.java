package com.example.newsfeedmanagementsystem.model;

import com.example.newsfeedmanagementsystem.exception.UnauthorizedActionException;
import com.example.newsfeedmanagementsystem.util.Session;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public abstract class User implements Serializable {
    private String username;
    private String passwordHash;
    private String displayName;
    private boolean isBanned = false;
    private Set<String> bookmarks = new HashSet<>();
    private static final long serialVersionUID = 1L;

    public User(String username, String passwordHash, String displayName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }

    public User(String username, String passwordHash, String displayName, Set<String> bookmarks) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.bookmarks = bookmarks;
    }

    public abstract boolean canPublish();

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) throws UnauthorizedActionException {
        User currentUser = Session.getCurrentUser();

        if (currentUser == null || !this.equals(currentUser)) {
            throw new UnauthorizedActionException("You are not allowed to perform this action");
        }
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isBanned() {
        return isBanned;
    }

    public void setBanned(boolean banned) throws UnauthorizedActionException {
        User currentUser = Session.getCurrentUser();

        if (currentUser == null || !(currentUser instanceof Admin)) {
            throw new UnauthorizedActionException("You are not allowed to perform this action");
        }
        isBanned = banned;
    }

    public boolean isBookmarked(String articleId) {
        return bookmarks.contains(articleId);
    }

    public void addBookmark(String articleId) throws UnauthorizedActionException {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null || !currentUser.username.equals(this.username))
            throw new UnauthorizedActionException("You are not allowed to perform this action");
        bookmarks.add(articleId);
    }

    public void removeBookmark(String articleId) throws UnauthorizedActionException {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null || !currentUser.username.equals(this.username))
            throw new UnauthorizedActionException("You are not allowed to perform this action");
        bookmarks.remove(articleId);
    }

    public void toggleBookmark(String articleId) throws UnauthorizedActionException {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null || !currentUser.username.equals(this.username))
            throw new UnauthorizedActionException("You are not allowed to perform this action");

        if (bookmarks.contains(articleId)) {
            bookmarks.remove(articleId);
        } else {
            bookmarks.add(articleId);
        }
    }

    public Set<String> getBookmarkIds() throws UnauthorizedActionException {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null || !currentUser.username.equals(this.username))
            throw new UnauthorizedActionException("You are not allowed to perform this action");
        return new HashSet<>(bookmarks);
    }

    public void updateUser(String displayName) throws UnauthorizedActionException {
        User currentUser = Session.getCurrentUser();

        if (currentUser == null || !currentUser.username.equals(this.username)) {
            throw new UnauthorizedActionException("You are not allowed to perform this action");
        }

        this.displayName = displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
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
