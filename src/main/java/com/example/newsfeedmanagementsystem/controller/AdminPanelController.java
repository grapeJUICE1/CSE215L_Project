package com.example.newsfeedmanagementsystem.controller;

import com.example.newsfeedmanagementsystem.exception.DuplicateUserException;
import com.example.newsfeedmanagementsystem.exception.UnauthorizedActionException;
import com.example.newsfeedmanagementsystem.exception.UserNotFoundException;
import com.example.newsfeedmanagementsystem.model.Admin;
import com.example.newsfeedmanagementsystem.model.Article;
import com.example.newsfeedmanagementsystem.model.Journalist;
import com.example.newsfeedmanagementsystem.model.User;
import com.example.newsfeedmanagementsystem.repository.ArticleRepository;
import com.example.newsfeedmanagementsystem.repository.UserRepository;
import com.example.newsfeedmanagementsystem.service.ModerationService;
import com.example.newsfeedmanagementsystem.util.AppState;
import com.example.newsfeedmanagementsystem.util.DialogUtils;
import com.example.newsfeedmanagementsystem.util.SceneManager;
import com.example.newsfeedmanagementsystem.util.ToastManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class AdminPanelController {
    @FXML
    private TableView<User> userTableView;
    @FXML
    private TableColumn<User, String> usernameCol;
    @FXML
    private TableColumn<User, String> displayNameCol;
    @FXML
    private TableColumn<User, String> roleCol;
    @FXML
    private TableColumn<User, String> statusCol;
    @FXML
    private TableColumn<User, Void> actionsCol;

    @FXML
    private ListView<Article> articleListView;
    @FXML
    private CheckBox showReportedOnlyCheckBox; // NEW

    private UserRepository userRepository;
    private ArticleRepository articleRepository;
    private ModerationService moderationService;

    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private final ObservableList<Article> articleList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        userRepository = new UserRepository();
        articleRepository = new ArticleRepository();
        userRepository.load();
        articleRepository.load();

        moderationService = new ModerationService(userRepository, articleRepository);

        setupUserTable();
        setupArticleList();
        showReportedOnlyCheckBox.setOnAction(e -> loadArticles());

        loadData();
    }

    private void setupUserTable() {
        usernameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        displayNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDisplayName()));
        roleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClass().getSimpleName()));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isBanned() ? "BANNED" : "Active"));

        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button banBtn = new Button();
            private final Button promoteBtn = new Button();
            private final HBox container = new HBox(8, banBtn, promoteBtn);

            {
                banBtn.getStyleClass().add("button-danger");
                promoteBtn.getStyleClass().add("button-secondary");

                banBtn.setOnAction(e -> {
                    e.consume();
                    User user = getTableView().getItems().get(getIndex());
                    handleToggleBan(user.getUsername());
                });

                promoteBtn.setOnAction(e -> {
                    e.consume();
                    User user = getTableView().getItems().get(getIndex());
                    if (user instanceof Journalist) {
                        handleDemote(user.getUsername());
                    } else if (!(user instanceof Admin)) {
                        handlePromote(user.getUsername());
                    }
                });
            }

            private void handleDemote(String username) {
                if (!DialogUtils.confirm("Demote User?",   "Demote " + username + " to Regular?", "This action can be reversed.")) {
                    return;
                }
                try {
                    moderationService.demoteToRegular(username);
                    ToastManager.success("User demoted to Regular User");
                    loadData();
                } catch (UnauthorizedActionException | UserNotFoundException | DuplicateUserException e) {
                    ToastManager.error(e.getMessage());
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    banBtn.setText(user.isBanned() ? "Unban" : "Ban");

                    if (user instanceof Admin) {
                        promoteBtn.setVisible(false);
                        promoteBtn.setManaged(false);
                    } else if (user instanceof Journalist) {
                        promoteBtn.setText("Demote to Regular");
                        promoteBtn.setVisible(true);
                        promoteBtn.setManaged(true);
                    } else {
                        promoteBtn.setText("Promote to Journalist");
                        promoteBtn.setVisible(true);
                        promoteBtn.setManaged(true);
                    }
                    setGraphic(container);
                }
            }
        });

        userTableView.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    User selectedUser = row.getItem();
                    AppState.setViewedProfileUser(selectedUser);
                    SceneManager.switchTo("profile");
                }
            });
            return row;
        });

        userTableView.setItems(userList);
    }

    private void setupArticleList() {
        articleListView.setCellFactory(list -> new ListCell<>() {
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

                    Label author = new Label("By: " + article.getAuthor().getDisplayName() + " (@" + article.getAuthor().getUsername() + ")");
                    author.getStyleClass().add("article-meta");

                    Button deleteBtn = new Button("Delete Article");
                    deleteBtn.getStyleClass().add("button-danger");
                    deleteBtn.setOnAction(e -> {
                        e.consume();
                        handleDeleteArticle(article);
                    });

                    card.getChildren().addAll(title, author, deleteBtn);

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

        articleListView.setItems(articleList);
    }

    private void loadData() {
        userList.setAll(userRepository.getAllUsers());
        loadArticles();
    }

    private void loadArticles() {
        List<Article> articles = null;
        if (showReportedOnlyCheckBox.isSelected()) {
            try {
                articles = moderationService.getReportedArticles();
            } catch (UnauthorizedActionException e) {
                ToastManager.error(e.getMessage());
            }
        } else {
            articles = articleRepository.getAllArticles();
        }
        articleList.setAll(articles);
    }

    private void handleToggleBan(String username) {
        try {
            if (!DialogUtils.confirm("Ban User",   "Ban " + username + "?", "This action can be reversed.")) {
                return;
            }
            moderationService.toggleBanStatus(username);
            ToastManager.success("User ban status updated!");
            loadData();
        } catch (UnauthorizedActionException | UserNotFoundException e) {
            ToastManager.error(e.getMessage());
        }
    }

    private void handlePromote(String username) {
        if (!DialogUtils.confirm("Promote User?",   "Promote " + username + " to Journalist?", "This action can be reversed.")) {
            return;
        }
        try {
            moderationService.promoteToJournalist(username);
            ToastManager.success("User promoted to Journalist!");
            loadData();
        } catch (UnauthorizedActionException | UserNotFoundException | DuplicateUserException e) {
            ToastManager.error(e.getMessage());
        }
    }

    private void handleDeleteArticle(Article article) {
        if (!DialogUtils.confirm("Delete Article", "Delete this article?", "This action cannot be undone.")) {
            return;
        }
        try {
            moderationService.deleteArticle(article);
            ToastManager.success("Article deleted successfully.");
            loadData();
        } catch (UnauthorizedActionException e) {
            ToastManager.error(e.getMessage());
        }
    }

    @FXML
    public void onBackClicked() {
        SceneManager.switchTo("feed");
    }
}