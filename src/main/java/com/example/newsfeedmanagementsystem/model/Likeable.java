package com.example.newsfeedmanagementsystem.model;

import com.example.newsfeedmanagementsystem.exception.UnauthorizedActionException;

public interface Likeable {

    boolean toggleLike() throws UnauthorizedActionException;

    boolean isLikedByCurrentUser();

    int getLikes();
}
