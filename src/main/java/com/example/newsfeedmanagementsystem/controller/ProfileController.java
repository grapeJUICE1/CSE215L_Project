package com.example.newsfeedmanagementsystem.controller;

import com.example.newsfeedmanagementsystem.exception.InvalidCredentialsException;
import com.example.newsfeedmanagementsystem.exception.UnauthorizedActionException;
import com.example.newsfeedmanagementsystem.exception.UserNotFoundException;
import com.example.newsfeedmanagementsystem.model.Admin;
import com.example.newsfeedmanagementsystem.model.Article;
import com.example.newsfeedmanagementsystem.model.Journalist;
import com.example.newsfeedmanagementsystem.model.User;
import com.example.newsfeedmanagementsystem.repository.ArticleRepository;
import com.example.newsfeedmanagementsystem.repository.UserRepository;
import com.example.newsfeedmanagementsystem.service.AuthService;
import com.example.newsfeedmanagementsystem.util.AppState;
import com.example.newsfeedmanagementsystem.util.SceneManager;
import com.example.newsfeedmanagementsystem.util.Session;
import com.example.newsfeedmanagementsystem.util.ToastManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

public class ProfileController {

    @FXML private Label headerLabel;
    @FXML private Label displayNameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label roleTag;
    @FXML private Label statusLabel;

    @FXML private VBox settingsContainer;
    @FXML private TextField editDisplayNameField;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;

    @FXML private VBox articlesContainer; // Wrap ListView and Header in a VBox in profile.fxml if needed, or toggle both
    @FXML private Label articlesHeaderLabel;
    @FXML private ListView<Article> userArticlesListView;

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

        if(clicked != null) {
            try {
                targetUser = userRepository.findUserByUsername(clicked.getUsername());
            } catch (UserNotFoundException e) {
                ToastManager.error(e.getMessage());
            }

        }

        if (targetUser  != null) {
            isSelfProfile = currentUser != null && currentUser.equals(clicked );
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
    }

    private boolean isPublishingRole(User user) {
        return user instanceof Journalist || user instanceof Admin;
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
            articlesHeaderLabel.setText("Your Articles");
        } else {
            headerLabel.setText(targetUser.getDisplayName() + "'s Profile");
            settingsContainer.setVisible(false);
            settingsContainer.setManaged(false);
            articlesHeaderLabel.setText("Articles by " + targetUser.getDisplayName());
        }

        boolean canPublish = isPublishingRole(targetUser);
        articlesHeaderLabel.setVisible(canPublish);
        articlesHeaderLabel.setManaged(canPublish);
        userArticlesListView.setVisible(canPublish);
        userArticlesListView.setManaged(canPublish);

        if (canPublish) {
            userArticlesListView.setCellFactory(list -> new ListCell<Article>() {
                @Override
                protected void updateItem(Article article, boolean empty) {
                    super.updateItem(article, empty);
                    if (empty || article == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        VBox card = new VBox(6);
                        card.getStyleClass().add("card");

                        Label title = new Label(article.getTitle());
                        title.getStyleClass().add("article-title");

                        Label tag = new Label(article.getCategory());
                        tag.getStyleClass().add("tag-chip");

                        Label meta = new Label(article.getLikes() + " likes • " + article.getComments().size() + " comments • " + article.getPublishedAt());
                        meta.getStyleClass().add("article-meta");

                        card.getChildren().addAll(title, tag, meta);
                        card.setOnMouseClicked(e -> {
                            AppState.setSelectedArticle(article);
                            AppState.setViewedProfileUser(null);
                            SceneManager.switchTo("article-detail");
                        });

                        setGraphic(card);
                        setText(null);
                    }
                }
            });
        }
    }

    private void loadUserArticles() {
        if (isPublishingRole(targetUser)) {
            List<Article> userArticles = articleRepository.getArticlesByUser(targetUser);
            userArticlesListView.getItems().clear();
            userArticlesListView.getItems().addAll(userArticles);
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