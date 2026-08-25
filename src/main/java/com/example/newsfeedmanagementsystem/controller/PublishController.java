package com.example.newsfeedmanagementsystem.controller;

import com.example.newsfeedmanagementsystem.model.Article;
import com.example.newsfeedmanagementsystem.model.BreakingNews;
import com.example.newsfeedmanagementsystem.model.Editorial;
import com.example.newsfeedmanagementsystem.model.User;
import com.example.newsfeedmanagementsystem.repository.ArticleRepository;
import com.example.newsfeedmanagementsystem.util.SceneManager;
import com.example.newsfeedmanagementsystem.util.Session;
import com.example.newsfeedmanagementsystem.util.ThemeManager;
import com.example.newsfeedmanagementsystem.util.ToastManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PublishController {
    @FXML
    private TextField titleField;
    @FXML
    private ComboBox<String> categoryBox;
    @FXML
    private ComboBox<String> typeBox;
    @FXML
    private TextArea contentField;
    @FXML
    private Label errorLabel;
    @FXML
    private ToggleButton darkModeToggle;

    private ArticleRepository articleRepository;

    @FXML
    public void initialize() {
        if (Session.getCurrentUser() == null || !Session.getCurrentUser().canPublish()) {
            ToastManager.error("You are not authorized to perform this action.");
            SceneManager.switchTo("feed");
        }
        articleRepository = new ArticleRepository();
        articleRepository.load();
        categoryBox.getItems().addAll("Weather", "Politics", "Sports", "Tech", "Business");
        categoryBox.editableProperty().setValue(true);
        typeBox.getItems().addAll("Breaking News", "Editorial");

    }

    @FXML
    public void onPublishClicked() {
        String title = titleField.getText();
        String category = categoryBox.getValue();
        String type = typeBox.getValue();
        String content = contentField.getText();

        if (title == null || title.length() == 0) {
            errorLabel.setText("Please enter a title");
            return;
        }
        if (category == null || category.length() == 0) {
            errorLabel.setText("Please enter a category");
            return;
        }
        if (type == null || type.length() == 0) {
            errorLabel.setText("Please enter a type");
            return;
        }
        if (content.length() == 0) {
            errorLabel.setText("Please enter content");
            return;
        }

        Article article;
        User author = Session.getCurrentUser();
        if (typeBox.getValue().equals("Breaking News")) {
            article = new BreakingNews(title, content, author, category);
        } else {
            article = new Editorial(title, content, author, category);
        }

        articleRepository.addArticle(article);
        articleRepository.save();
        ToastManager.success("Successfully published article");
        SceneManager.switchTo("feed");
    }

    @FXML
    public void onDarkModeToggleClicked() {
        ThemeManager.toggleTheme(darkModeToggle.getScene());
    }

    @FXML
    public void onBackClicked() {
        SceneManager.switchTo("feed");
    }
}
