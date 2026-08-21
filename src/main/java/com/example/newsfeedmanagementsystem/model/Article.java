package com.example.newsfeedmanagementsystem.model;

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
    private static final long serialVersionUID = 1L;
    private List<Comment> comments;

    public Article(String title, String content, User author, String category) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.category = category;

        this.comments = new ArrayList<>();
    }
    public abstract String render();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
            return content;
        }

    public void setContent(String content) {
        this.content = content;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    public int getLikes() {
        return likes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    @Override
    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    @Override
    public void like() {
        this.likes++;
    }
    @Override
    public void unlike() {
        if (this.likes > 0 )
            this.likes--;
    }

    @Override
    public String toString() {
        return String.format("[%s] \"%s\" by %s — %d likes, %d comments (%s)",
                category, title, author.getUsername(), likes, comments.size(), getClass().getSimpleName());
    }
}
