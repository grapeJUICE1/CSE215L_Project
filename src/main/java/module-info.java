module com.example.newsfeedmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.newsfeedmanagementsystem to javafx.fxml;
    opens com.example.newsfeedmanagementsystem.controller to javafx.fxml;

    exports com.example.newsfeedmanagementsystem;
    exports com.example.newsfeedmanagementsystem.model;
    exports com.example.newsfeedmanagementsystem.controller;
    exports com.example.newsfeedmanagementsystem.service;
    exports com.example.newsfeedmanagementsystem.repository;
    exports com.example.newsfeedmanagementsystem.exception;
    exports com.example.newsfeedmanagementsystem.util;
}