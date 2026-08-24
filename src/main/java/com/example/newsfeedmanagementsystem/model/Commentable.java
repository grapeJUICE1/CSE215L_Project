package com.example.newsfeedmanagementsystem.model;

import com.example.newsfeedmanagementsystem.exception.UnauthorizedActionException;

public interface Commentable {
    void addComment(Comment comment) throws UnauthorizedActionException;

    void removeComment(Comment comment) throws UnauthorizedActionException;
}
