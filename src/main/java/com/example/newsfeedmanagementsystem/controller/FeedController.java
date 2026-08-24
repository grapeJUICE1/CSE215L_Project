package com.example.newsfeedmanagementsystem.controller;

import com.example.newsfeedmanagementsystem.model.Admin;
import com.example.newsfeedmanagementsystem.model.Article;
import com.example.newsfeedmanagementsystem.model.BreakingNews;
import com.example.newsfeedmanagementsystem.repository.ArticleRepository;
import com.example.newsfeedmanagementsystem.repository.UserRepository;
import com.example.newsfeedmanagementsystem.service.FeedService;
import com.example.newsfeedmanagementsystem.util.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class FeedController {
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> categoryFilterBox;
    @FXML
    private ComboBox<String> sortModeBox;
    @FXML
    private ListView<Article> articleListView;
    @FXML
    private Button publishButton;
    @FXML
    private Label pageLabel;
    @FXML
    private Hyperlink adminLink;
    @FXML
    private ComboBox<String> typeFilterBox;
    @FXML
    private Button prevPageButton;
    @FXML
    private Button nextPageButton;

    private UserRepository userRepository;
    private ArticleRepository articleRepository;
    private FeedService feedService;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 5;


    @FXML
    public void initialize() {
        userRepository = new UserRepository();
        articleRepository = new ArticleRepository();
        userRepository.load();
        articleRepository.load();
        feedService = new FeedService();
        categoryFilterBox.getItems().addAll(feedService.getAllCategories(articleRepository.getAllArticles()));
        typeFilterBox.getItems().addAll("All", "Breaking News", "Editorial");
        typeFilterBox.setValue("All");
        typeFilterBox.setOnAction(e -> onFilterChanged());
        sortModeBox.getItems().addAll("recency", "engagement");
        sortModeBox.setValue("recency");

        boolean canPublish = Session.getCurrentUser() != null && Session.getCurrentUser().canPublish();
        System.out.println(canPublish);
        publishButton.setVisible(canPublish);
        publishButton.setManaged(canPublish);

        boolean isAdmin = Session.getCurrentUser() != null && Session.getCurrentUser() instanceof Admin;
        adminLink.setVisible(isAdmin);
        adminLink.setManaged(isAdmin);

        articleListView.setCellFactory(list -> new ListCell<Article>() {
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

                    HBox metaContainer = new HBox(6);
                    metaContainer.setAlignment(Pos.CENTER_LEFT);

                    Label authorLabel = new Label("By " + article.getAuthor().getDisplayName());
                    authorLabel.getStyleClass().add("author-link");

                    authorLabel.setOnMouseClicked(e -> {
                        e.consume();
                        AppState.setViewedProfileUser(article.getAuthor());
                        SceneManager.switchTo("profile");
                    });

                    Label statsLabel = new Label(" • " + article.getLikes() + " likes • " + article.getComments().size() + " comments");
                    statsLabel.getStyleClass().add("article-meta");

                    Label dateLabel = new Label(DateUtils.format(article.getPublishedAt()));
                    dateLabel.getStyleClass().add("article-meta");

                    metaContainer.getChildren().addAll(authorLabel, statsLabel, dateLabel);

                    Label tag = new Label(article.getCategory());
                    tag.getStyleClass().add("tag-chip");

                    Label typeBadge = new Label(article.getTypeName());
                    typeBadge.getStyleClass().add("tag-chip");
                    if (article instanceof BreakingNews) {
                        typeBadge.setStyle("-fx-background-color: #e5484d; -fx-text-fill: white;");
                    } else {
                        typeBadge.setStyle("-fx-background-color: #4f9de8; -fx-text-fill: white;");
                    }

                    String contentSnippet = article.getContent().length() > 120
                            ? article.getContent().substring(0, 120) + "..."
                            : article.getContent();

                    Label snippet = new Label(contentSnippet);
                    snippet.getStyleClass().add("article-body");
                    snippet.setWrapText(true);

                    card.getChildren().addAll(title, metaContainer, tag, typeBadge, snippet);
                    card.setOnMouseClicked(e -> onArticleClicked(article));

                    setGraphic(card);
                    setText(null);
                }
            }
        });
        refreshFeed();
    }

    private void updatePaginationButtons() {
        prevPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable(articleListView.getItems().size() < PAGE_SIZE);
    }

    private void refreshFeed() {
        String search = searchField.getText();
        String category = categoryFilterBox.getValue();
        String sort = sortModeBox.getValue();
        String type = typeFilterBox.getValue();

        List<Article> feed = feedService.getFeed(articleRepository.getAllArticles(),
                search, category, null, sort, currentPage, PAGE_SIZE, type);
        ;
        articleListView.getItems().clear();
        articleListView.getItems().addAll(feed);
        updatePaginationButtons();
        pageLabel.setText("Page " + (currentPage + 1));
    }

    @FXML
    public void onSearchChanged() {
        currentPage = 0;
        refreshFeed();
    }

    @FXML
    public void onFilterChanged() {
        currentPage = 0;
        refreshFeed();
    }

    @FXML
    public void onSortChanged() {
        currentPage = 0;
        refreshFeed();
    }

    @FXML
    public void onPrevPageClicked() {
        if (currentPage > 0) {
            currentPage--;
            refreshFeed();
        }
    }

    @FXML
    public void onNextPageClicked() {
        if (articleListView.getItems().size() < PAGE_SIZE) {
            return;
        }
        currentPage++;
        refreshFeed();
    }

    @FXML
    public void onPublishClicked() {
        SceneManager.switchTo("publish");
    }

    @FXML
    public void onProfileClicked() {
        AppState.setViewedProfileUser(null);
        SceneManager.switchTo("profile");
    }

    @FXML
    public void onAdminClicked() {
        SceneManager.switchTo("admin-panel");
    }

    @FXML
    public void onLogoutClicked() {
        Session.logout();
        ToastManager.success("Logged out successfully");
        SceneManager.switchTo("login");
    }

    @FXML
    public void onArticleClicked(Article article) {
        AppState.setSelectedArticle(article);
        SceneManager.switchTo("article-detail");
    }

    @FXML
    public void onResetFiltersClicked() {
        searchField.clear();
        categoryFilterBox.setValue(null);
        sortModeBox.setValue(null);
        typeFilterBox.setValue("All");
        currentPage = 0;
        refreshFeed();
    }

    @FXML
    public void onDarkModeToggleClicked() {
        ThemeManager.toggleTheme(searchField.getScene());
    }
}
