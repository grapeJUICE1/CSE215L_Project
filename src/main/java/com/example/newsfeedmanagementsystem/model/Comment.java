package com.example.newsfeedmanagementsystem.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Comment implements Serializable,Commentable {
    private User author;
    private String content;
    private Date timestamp = new Date();
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

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public List<Comment> getReplies() {
        return replies;
    }

    @Override
    public void addComment(Comment comment) {
        replies.add(comment);
    }

    @Override
    public String toString() {
        return String.format("%s: \"%s\" (%d replies)", author.getUsername(), content, replies.size());
    }
}
