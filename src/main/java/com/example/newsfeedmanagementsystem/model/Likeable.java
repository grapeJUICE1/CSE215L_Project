package com.example.newsfeedmanagementsystem.model;

import com.example.newsfeedmanagementsystem.exception.UnauthorizedActionException;

import java.util.Set;

public interface Likeable {

    private Set<String> getLikedBy() {
        return null;
    }

    public boolean toggleLike() throws UnauthorizedActionException;

    public boolean isLikedByCurrentUser();

    public int getLikes();
}
