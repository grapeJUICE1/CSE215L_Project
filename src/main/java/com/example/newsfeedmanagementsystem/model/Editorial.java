package com.example.newsfeedmanagementsystem.model;

public class Editorial extends Article {
    public Editorial(String title, String content, User author, String category) {
        super(title, content, author, category);
    }

    @Override
    public String render() {
        return "editorial";
    }
}
