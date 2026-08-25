package com.example.newsfeedmanagementsystem.model;

import com.example.newsfeedmanagementsystem.exception.UnauthorizedActionException;
import com.example.newsfeedmanagementsystem.util.Session;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public abstract class Article implements Serializable, Likeable, Commentable {
    private String title;
    private String content;
    private User author;
    private String category;
    private Date publishedAt = new Date();
    private int likes;
    private int reportCount;
    private Set<String> likedBy;
    private Set<String> reportedBy;
    @Serial
    private static final long serialVersionUID = 1L;
    private List<Comment> comments;
    private String id;

    public Article(String title, String content, User author, String category) {
        this.id = java.util.UUID.randomUUID().toString();
        this.title = title;
        this.content = content;
        this.author = author;
        this.category = category;

        this.comments = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    private Set<String> getLikedBy() {
        if (likedBy == null) likedBy = new HashSet<>();
        return likedBy;
    }

    private Set<String> getReportedBy() {
        if (reportedBy == null) reportedBy = new HashSet<>();
        return reportedBy;
    }

    public boolean isLikedByCurrentUser() {
        User current = Session.getCurrentUser();
        return current != null && getLikedBy().contains(current.getUsername());
    }

    public boolean isReportedByCurrentUser() {
        User current = Session.getCurrentUser();
        return current != null && getReportedBy().contains(current.getUsername());
    }

    public boolean toggleLike() throws UnauthorizedActionException {
        User current = Session.getCurrentUser();
        if (current == null) throw new UnauthorizedActionException("You are not logged in");
        String username = current.getUsername();
        Set<String> liked = getLikedBy();
        if (liked.contains(username)) {
            liked.remove(username);
            if (this.likes > 0) this.likes--;
            return false;
        } else {
            liked.add(username);
            this.likes++;
            return true;
        }
    }

    public boolean report() {
        User current = Session.getCurrentUser();
        if (current == null) return false;
        String username = current.getUsername();
        Set<String> reported = getReportedBy();
        if (!reported.contains(username)) {
            reported.add(username);
            this.reportCount++;
            return true;
        }
        return false;
    }

    public String getTypeName() {
        return this instanceof BreakingNews ? "Breaking News" : "Editorial";
    }

    public Article convertToType(String newType) {
        Article newArticle;
        if (newType.equals("Breaking News")) {
            newArticle = new BreakingNews(this.title, this.content, this.author, this.category);
        } else if (newType.equals("Editorial")) {
            newArticle = new Editorial(this.title, this.content, this.author, this.category);
        } else {
            throw new IllegalArgumentException("Invalid type: " + newType);
        }
        newArticle.setId(this.id);
        newArticle.publishedAt = this.publishedAt;
        newArticle.likes = this.likes;
        newArticle.reportCount = this.reportCount;
        newArticle.comments = new ArrayList<>(this.comments);
        newArticle.likedBy = new HashSet<>(this.getLikedBy());
        newArticle.reportedBy = new HashSet<>(this.getReportedBy());
        return newArticle;
    }


    @Override
    public void addComment(Comment comment) throws UnauthorizedActionException {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null || !currentUser.equals(comment.getAuthor()))
            throw new UnauthorizedActionException("You are not allowed to perform this action");

        this.comments.add(comment);
    }

    @Override
    public void removeComment(Comment comment) throws UnauthorizedActionException {

        User currentUser = Session.getCurrentUser();
        if (currentUser == null)
            throw new UnauthorizedActionException("You are not allowed to perform this action");

        if (!comment.getAuthor().equals(currentUser) && !(currentUser instanceof Admin)) {
            throw new UnauthorizedActionException("Not allowed to delete comment");
        }
        comments.remove(comment);
    }


    public void update(String newTitle, String newContent, String newCategory) throws UnauthorizedActionException {
        User currentUser = Session.getCurrentUser();
        if (!this.author.equals(currentUser)) {
            throw new UnauthorizedActionException("Only the original author can edit this article");
        }
        this.title = newTitle;
        this.content = newContent;
        this.category = newCategory;
    }


    @Override
    public String toString() {
        return String.format("[%s] \"%s\" by %s — %d likes, %d comments (%s)",
                category, title, author.getUsername(), likes, comments.size(), getClass().getSimpleName());
    }
}
