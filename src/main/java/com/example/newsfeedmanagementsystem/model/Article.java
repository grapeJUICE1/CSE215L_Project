package com.example.newsfeedmanagementsystem.model;

import com.example.newsfeedmanagementsystem.exception.UnauthorizedActionException;
import com.example.newsfeedmanagementsystem.util.Session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class Article implements Serializable, Likeable,Commentable{
    private String title;
    private String content;
    private User author;
    private String category;
    private Date publishedAt = new Date();
    private int likes;
    private int reportCount;
    private static final long serialVersionUID = 1L;
    private List<Comment> comments;
    private final String id;

    public Article(String title, String content, User author, String category) {
        this.id = java.util.UUID.randomUUID().toString();
        this.title = title;
        this.content = content;
        this.author = author;
        this.category = category;

        this.comments = new ArrayList<>();
    }
    public abstract String render();

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
            return content;
        }

    public User getAuthor() {
        return author;
    }

    public String getCategory() {
        return category;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public int getLikes() {
        return likes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public int getReportCount() {
       return reportCount;
    }

    public void report() {
        this.reportCount++;
    }

    @Override
    public void addComment(Comment comment) throws UnauthorizedActionException{
        User currentUser = Session.getCurrentUser();
        if(currentUser == null || !currentUser.equals(comment.getAuthor()))
            throw new UnauthorizedActionException("You are not allowed to perform this action");

        this.comments.add(comment);
    }

    @Override
    public void removeComment(Comment comment) throws UnauthorizedActionException {

        User currentUser = Session.getCurrentUser();
        if(currentUser == null)
            throw new UnauthorizedActionException("You are not allowed to perform this action");

        if(!comment.getAuthor().equals(currentUser) && !(currentUser instanceof Admin)) {
           throw new UnauthorizedActionException("Not allowed to delete comment");
        }
        comments.remove(comment);
    }


    public void update(String newTitle, String newContent , String newCategory) throws UnauthorizedActionException {
        User currentUser = Session.getCurrentUser();
        if (!this.author.equals(currentUser)) {
            throw new UnauthorizedActionException("Only the original author can edit this article");
        }
        this.title = newTitle;
        this.content = newContent;
        this.category = newCategory;
    }

    @Override
    public void like() throws UnauthorizedActionException{
       User currentUser = Session.getCurrentUser();
       if(currentUser == null)
           throw new UnauthorizedActionException("You are not allowed to perform this action");
        this.likes++;
    }
    @Override
    public void unlike() throws UnauthorizedActionException {
        User currentUser = Session.getCurrentUser();
        if(currentUser == null)
            throw new UnauthorizedActionException("You are not allowed to perform this action");

        if (this.likes > 0 )
            this.likes--;
    }

    @Override
    public String toString() {
        return String.format("[%s] \"%s\" by %s — %d likes, %d comments (%s)",
                category, title, author.getUsername(), likes, comments.size(), getClass().getSimpleName());
    }
}
