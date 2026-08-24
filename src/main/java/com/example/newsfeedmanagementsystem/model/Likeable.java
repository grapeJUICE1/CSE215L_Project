package com.example.newsfeedmanagementsystem.model;

import com.example.newsfeedmanagementsystem.exception.UnauthorizedActionException;

public interface Likeable {
    public void like() throws UnauthorizedActionException;
    public void unlike() throws UnauthorizedActionException;
}
