package com.example.newsfeedmanagementsystem.model;

import com.example.newsfeedmanagementsystem.exception.UnauthorizedActionException;
import com.example.newsfeedmanagementsystem.util.Session;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Comment implements Serializable, Commentable {
    private User author;
    private String content;
    private Date timestamp = new Date();
    @Serial
    private static final long serialVersionUID = 1L;
    private List<Comment> replies;

    public Comment(User author, String content) {
        this.author = author;
        this.content = content;

        this.replies = new ArrayList<>();
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) throws UnauthorizedActionException {
        User currentUser = Session.getCurrentUser();

        if (!(currentUser instanceof Admin)) {
            throw new UnauthorizedActionException("You are not allowed to perform this action");
        }

        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public List<Comment> getReplies() {
        return replies;
    }

    @Override
    public void addComment(Comment comment) throws UnauthorizedActionException {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null || !currentUser.equals(comment.getAuthor()))
            throw new UnauthorizedActionException("You are not allowed to perform this action");

        this.replies.add(comment);
    }

    @Override
    public void removeComment(Comment comment) throws UnauthorizedActionException {
        User currentUser = Session.getCurrentUser();

        if (currentUser == null)
            throw new UnauthorizedActionException("You are not allowed to perform this action");

        if (!comment.getAuthor().equals(currentUser) && !(currentUser instanceof Admin)) {
            throw new UnauthorizedActionException("Not allowed to delete comment");
        }
        replies.remove(comment);
    }

    @Override
    public String toString() {
        return String.format("%s: \"%s\" (%d replies)", author.getUsername(), content, replies.size());
    }
}
