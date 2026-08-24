package com.example.newsfeedmanagementsystem.model;

import java.io.Serializable;
import java.util.Date;

public class Notification implements Serializable {
    User recipient;
    String message;
    Date timestamp = new Date();
    private static final long serialVersionUID = 1L;
    private boolean isRead;

    public Notification(User recipient, String message){
        this.recipient = recipient;
        this.message = message;
        this.isRead = false;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public String toString() {
        return String.format("[%s] To: %s — %s", isRead ? "READ" : "UNREAD", recipient.getUsername(), message);
    }
}
