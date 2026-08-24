package com.example.newsfeedmanagementsystem.controller;

import com.example.newsfeedmanagementsystem.exception.InvalidCredentialsException;
import com.example.newsfeedmanagementsystem.exception.UnauthorizedActionException;
import com.example.newsfeedmanagementsystem.exception.UserNotFoundException;
import com.example.newsfeedmanagementsystem.model.*;
import com.example.newsfeedmanagementsystem.repository.ArticleRepository;
import com.example.newsfeedmanagementsystem.repository.UserRepository;
import com.example.newsfeedmanagementsystem.service.AuthService;
import com.example.newsfeedmanagementsystem.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProfileController {

    @FXML
    private Label headerLabel;
    @FXML
    private Label displayNameLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label roleTag;
    @FXML
    private Label statusLabel;
    @FXML
    private VBox settingsContainer;
    @FXML
    private TextField editDisplayNameField;
    @FXML
    private PasswordField oldPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private ListView<Article> userArticlesListView;
    @FXML
    private ListView<Article> bookmarksListView;
    @FXML
    private TabPane profileTabPane;

    private UserRepository userRepository;
    private ArticleRepository articleRepository;
    private AuthService authService;
    private User targetUser;
    private boolean isSelfProfile;

    @FXML
    public void initialize() {
        userRepository = new UserRepository();
        articleRepository = new ArticleRepository();
        userRepository.load();
        articleRepository.load();
        authService = new AuthService(userRepository);

        User clicked = AppState.getViewedProfileUser();
        User currentUser = Session.getCurrentUser();

        if (clicked != null) {
            try {
                targetUser = userRepository.findUserByUsername(clicked.getUsername());
            } catch (UserNotFoundException e) {
                ToastManager.error(e.getMessage());
            }
        }

        if (targetUser != null) {
            isSelfProfile = currentUser != null && currentUser.equals(clicked);
        } else if (currentUser != null) {
            targetUser = currentUser;
            isSelfProfile = true;
        } else {
            ToastManager.error("Please log in to view your profile.");
            SceneManager.switchTo("login");
            return;
        }

        setupUI();
        loadUserArticles();
        setupBookmarksTab();
    }

    private void setupUI() {
        displayNameLabel.setText(targetUser.getDisplayName());
        usernameLabel.setText("@" + targetUser.getUsername());
        roleTag.setText(targetUser.getClass().getSimpleName());

        if (targetUser.isBanned()) {
            statusLabel.setText("ACCOUNT BANNED");
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
        }

        if (isSelfProfile) {
            headerLabel.setText("My Profile");
            settingsContainer.setVisible(true);
            settingsContainer.setManaged(true);
            editDisplayNameField.setText(targetUser.getDisplayName());
        } else {
            headerLabel.setText(targetUser.getDisplayName() + "'s Profile");
            settingsContainer.setVisible(false);
            settingsContainer.setManaged(false);
        }

        boolean canPublish = targetUser.canPublish();
        userArticlesListView.setVisible(canPublish);
        userArticlesListView.setManaged(canPublish);

        if (canPublish) {
            userArticlesListView.setCellFactory(list -> new ListCell<Article>() {
                @Override
                protected void updateItem(Article article, boolean empty) {
                    super.updateItem(article, empty);
                    if (empty || article == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        VBox card = buildArticleCard(article);
                        setGraphic(card);
                    }
                }
            });
        }
    }

    private VBox buildArticleCard(Article article) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");

        Label title = new Label(article.getTitle());
        title.getStyleClass().add("article-title");

        Label tag = new Label(article.getCategory());
        tag.getStyleClass().add("tag-chip");

        Label meta = new Label(article.getLikes() + " likes • " + article.getComments().size() + " comments • " + DateUtils.format(article.getPublishedAt()));
        meta.getStyleClass().add("article-meta");

        card.getChildren().addAll(title, tag, meta);
        card.setOnMouseClicked(e -> {
            AppState.setSelectedArticle(article);
            AppState.setViewedProfileUser(null);
            SceneManager.switchTo("article-detail");
        });
        return card;
    }

    private void loadUserArticles() {
        if (targetUser.canPublish()) {
            List<Article> userArticles = articleRepository.getArticlesByUser(targetUser);
            userArticlesListView.getItems().setAll(userArticles);
        }
    }

    private void setupBookmarksTab() {
        if (isSelfProfile) {
            loadUserArticles();
            bookmarksListView.setCellFactory(list -> new ListCell<Article>() {
                @Override
                protected void updateItem(Article article, boolean empty) {
                    super.updateItem(article, empty);
                    if (empty || article == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        VBox card = buildArticleCard(article);
                        setGraphic(card);
                    }
                }
            });
            loadBookmarks();

            profileTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab.getText().equals("Bookmarks")) {
                    loadBookmarks();
                }
            });
        } else {
            profileTabPane.getTabs().removeIf(tab -> tab.getText().equals("Bookmarks"));
        }
    }

    private void loadBookmarks() {
        if (Session.getCurrentUser() == null) return;
        try {
            Set<String> ids = Session.getCurrentUser().getBookmarkIds();
            List<Article> bookmarked = articleRepository.getAllArticles().stream()
                    .filter(a -> ids.contains(a.getId()))
                    .collect(Collectors.toList());
            bookmarksListView.getItems().setAll(bookmarked);
        } catch (UnauthorizedActionException e) {
            ToastManager.error(e.getMessage());
        }
    }

    @FXML
    public void onUpdateDisplayNameClicked() {
        String newName = editDisplayNameField.getText();
        if (newName == null || newName.isBlank()) {
            ToastManager.error("Display name cannot be empty");
            return;
        }
        try {
            targetUser.updateUser(newName);
            userRepository.save();
            displayNameLabel.setText(newName);
            ToastManager.success("Display name updated successfully!");
        } catch (UnauthorizedActionException e) {
            ToastManager.error(e.getMessage());
        }
    }

    @FXML
    public void onChangePasswordClicked() {
        String oldPass = oldPasswordField.getText();
        String newPass = newPasswordField.getText();
        if (oldPass.isBlank() || newPass.isBlank()) {
            ToastManager.error("Please fill in both password fields");
            return;
        }
        try {
            authService.changePassword(oldPass, newPass);
            oldPasswordField.clear();
            newPasswordField.clear();
            ToastManager.success("Password changed successfully!");
        } catch (InvalidCredentialsException | UnauthorizedActionException e) {
            ToastManager.error(e.getMessage());
        }
    }

    @FXML
    public void onBackClicked() {
        AppState.setViewedProfileUser(null);
        SceneManager.switchTo("feed");
    }
}