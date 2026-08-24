package com.example.newsfeedmanagementsystem.controller;

import com.example.newsfeedmanagementsystem.model.Admin;
import com.example.newsfeedmanagementsystem.model.Article;
import com.example.newsfeedmanagementsystem.repository.ArticleRepository;
import com.example.newsfeedmanagementsystem.repository.UserRepository;
import com.example.newsfeedmanagementsystem.service.FeedService;
import com.example.newsfeedmanagementsystem.util.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Box;

import java.util.List;

public class FeedController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilterBox;
    @FXML private ComboBox<String> sortModeBox;
    @FXML private ListView<Article> articleListView;
    @FXML private Button publishButton;
    @FXML private Label pageLabel;
    @FXML private Hyperlink adminLink;


    private UserRepository userRepository;
    private ArticleRepository articleRepository;
    private FeedService feedService;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 5;


    @FXML public void initialize() {
       userRepository = new UserRepository();
       articleRepository = new ArticleRepository();
       userRepository.load();
       articleRepository.load();
       feedService = new FeedService();
       categoryFilterBox.getItems().addAll(feedService.getAllCategories(articleRepository.getAllArticles()));
       sortModeBox.getItems().addAll("recency", "engagement");

       boolean canPublish = Session.getCurrentUser() != null && Session.getCurrentUser().canPublish();
       System.out.println(canPublish);
       publishButton.setVisible(canPublish);
       publishButton.setManaged(canPublish);

       boolean isAdmin = Session.getCurrentUser() != null && Session.getCurrentUser() instanceof Admin;
       adminLink.setVisible(isAdmin);
       adminLink.setManaged(isAdmin);

       articleListView.setCellFactory(list->new ListCell<Article>(){
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
                       e.consume(); // Prevent launching the main article detail view
                       AppState.setViewedProfileUser(article.getAuthor());
                       SceneManager.switchTo("profile");
                   });

                   Label statsLabel = new Label(" • " + article.getLikes() + " likes • " + article.getComments().size() + " comments");
                   statsLabel.getStyleClass().add("article-meta");
                   metaContainer.getChildren().addAll(authorLabel, statsLabel);

                   Label tag = new Label(article.getCategory());
                   tag.getStyleClass().add("tag-chip");

                   String contentSnippet = article.getContent().length() > 120
                           ? article.getContent().substring(0, 120) + "..."
                           : article.getContent();

                   Label snippet = new Label(contentSnippet);
                   snippet.getStyleClass().add("article-body");
                   snippet.setWrapText(true);

                   // Added metaContainer into card children list
                   card.getChildren().addAll(title, metaContainer, tag, snippet);
                   card.setOnMouseClicked(e -> onArticleClicked(article));

                   setGraphic(card);
                   setText(null);
               }
           }
       });
       refreshFeed();
    }

    private void refreshFeed() {
        String search = searchField.getText();
        String category = categoryFilterBox.getValue();
        String sort = sortModeBox.getValue();

        List<Article> feed =feedService.getFeed(articleRepository.getAllArticles(),search,category,null,sort,currentPage,PAGE_SIZE);;
        articleListView.getItems().clear();
        articleListView.getItems().addAll(feed);
        pageLabel.setText("Page " + (currentPage+1));
    }

    @FXML public void onSearchChanged(){
        currentPage = 0;
        refreshFeed();
    }
    @FXML public void onFilterChanged(){
        currentPage = 0;
        refreshFeed();
    }
    @FXML public void onSortChanged(){
        currentPage = 0;
        refreshFeed();
    }
    @FXML public void onPrevPageClicked(){
        if(currentPage > 0)
            currentPage--;
        refreshFeed();
    }
    @FXML public void onNextPageClicked(){
        List<Article> currentView = articleListView.getItems();
        if(!currentView.isEmpty()) {
            currentPage++;
            refreshFeed();
        }
    }
    @FXML public void onPublishClicked(){
        SceneManager.switchTo("publish");
    }

    @FXML public void onProfileClicked(){
        AppState.setViewedProfileUser(null);
        SceneManager.switchTo("profile");
    }
    @FXML public void onAdminClicked(){
        SceneManager.switchTo("admin");
    }
    @FXML public void onLogoutClicked(){
        Session.logout();
        ToastManager.success("Logged out successfully");
        SceneManager.switchTo("login");
    }
    @FXML public void onArticleClicked(Article article){
        AppState.setSelectedArticle(article);
        SceneManager.switchTo("article-detail");
    }

    @FXML public void onDarkModeToggleClicked() {
        ThemeManager.toggleTheme(searchField.getScene());
    }
}
