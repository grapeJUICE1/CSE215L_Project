package com.example.newsfeedmanagementsystem.controller;

import com.example.newsfeedmanagementsystem.exception.UnauthorizedActionException;
import com.example.newsfeedmanagementsystem.exception.UserNotFoundException;
import com.example.newsfeedmanagementsystem.model.*;
import com.example.newsfeedmanagementsystem.repository.ArticleRepository;
import com.example.newsfeedmanagementsystem.repository.UserRepository;
import com.example.newsfeedmanagementsystem.service.ModerationService;
import com.example.newsfeedmanagementsystem.util.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ArticleDetailController {

    @FXML
    private Label categoryTag;
    @FXML
    private Label titleLabel;
    @FXML
    private Label contentLabel;
    @FXML
    private Label likeCountLabel;
    @FXML
    private TextArea newCommentField;
    @FXML
    private VBox commentsContainer;
    @FXML
    private Button likeButton;
    @FXML
    private Button reportButton;
    @FXML
    private Button bookmarkButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button changeTypeButton;
    @FXML
    private HBox metaContainer;
    @FXML
    private ToggleButton darkModeToggle;

    private ArticleRepository articleRepository;
    private UserRepository userRepository;
    private ModerationService moderationService;
    private Article article;

    @FXML
    public void initialize() {
        articleRepository = new ArticleRepository();
        userRepository = new UserRepository();
        articleRepository.load();
        userRepository.load();
        moderationService = new ModerationService(userRepository, articleRepository);

        Article clicked = AppState.getSelectedArticle();
        if (clicked == null) {
            ToastManager.error("No article selected");
            SceneManager.switchTo("feed");
            return;
        }
        article = articleRepository.findById(clicked.getId());
        if (article == null) {
            ToastManager.error("Article not found");
            SceneManager.switchTo("feed");
            return;
        }

        User current = Session.getCurrentUser();
        boolean isAuthor = current != null && current.equals(article.getAuthor());
        boolean isAdmin = current instanceof Admin;

        editButton.setVisible(isAuthor);
        editButton.setManaged(isAuthor);

        deleteButton.setVisible(isAuthor || isAdmin);
        deleteButton.setManaged(isAuthor || isAdmin);

        changeTypeButton.setVisible(isAdmin);
        changeTypeButton.setManaged(isAdmin);
        changeTypeButton.setOnAction(e -> onChangeTypeClicked());

        updateLikeButton();
        updateReportButton();
        updateBookmarkButton();

        renderArticle();
    }

    private void updateLikeButton() {
        if (Session.getCurrentUser() == null) {
            likeButton.setText("🤍 Like");
            likeButton.setDisable(true);
            return;
        }
        boolean liked = article.isLikedByCurrentUser();
        likeButton.setText(liked ? "❤️ Unlike" : "🤍 Like");
        likeButton.setDisable(false);
    }

    private void updateReportButton() {
        if (Session.getCurrentUser() == null) {
            reportButton.setDisable(true);
            reportButton.setText("Report");
            return;
        }
        boolean reported = article.isReportedByCurrentUser();
        reportButton.setText(reported ? "Reported" : "Report");
        reportButton.setDisable(reported);
    }

    private void updateBookmarkButton() {
        if (Session.getCurrentUser() == null) {
            bookmarkButton.setDisable(true);
            bookmarkButton.setText("☆ Bookmark");
            return;
        }
        boolean bookmarked = Session.getCurrentUser().isBookmarked(article.getId());
        bookmarkButton.setText(bookmarked ? "★ Unbookmark" : "☆ Bookmark");
    }

    private void renderArticle() {
        categoryTag.setText(article.getCategory());
        titleLabel.setText(article.getTitle());

        metaContainer.setAlignment(Pos.CENTER_LEFT);

        Label authorLabel = new Label("By " + article.getAuthor().getDisplayName());
        authorLabel.getStyleClass().add("author-link");
        authorLabel.setOnMouseClicked(e -> {
            AppState.setViewedProfileUser(article.getAuthor());
            SceneManager.switchTo("profile");
        });

        Label timestampLabel = new Label(" • " + DateUtils.format(article.getPublishedAt()));
        timestampLabel.getStyleClass().add("article-meta");
        metaContainer.getChildren().addAll(authorLabel, timestampLabel);

        contentLabel.setText(article.getContent());
        likeCountLabel.setText(article.getLikes() + " likes");
        renderComments();
    }

    private void renderComments() {
        commentsContainer.getChildren().clear();
        for (Comment comment : article.getComments()) {
            commentsContainer.getChildren().add(buildCommentNode(comment, 0, null));
        }
    }

    private VBox buildCommentNode(Comment comment, int depth, Comment parent) {
        VBox box = new VBox(6);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(10, 10, 10, 10 + depth * 24));

        Label author = new Label(comment.getAuthor().getDisplayName());
        author.getStyleClass().add("author-link");
        author.setOnMouseClicked(e -> {
            AppState.setViewedProfileUser(comment.getAuthor());
            SceneManager.switchTo("profile");
        });

        Label content = new Label(comment.getContent());
        content.getStyleClass().add("article-body");
        content.setWrapText(true);

        Label meta = new Label(DateUtils.format(comment.getTimestamp()));
        meta.getStyleClass().add("article-meta");

        Button replyButton = new Button("Reply");
        replyButton.getStyleClass().add("nav-link");
        replyButton.setOnAction(e -> onReplyClicked(comment));

        Button deleteButton = new Button("✕");
        deleteButton.getStyleClass().add("button-danger");
        User current = Session.getCurrentUser();
        boolean canDelete = current != null && (current.equals(comment.getAuthor()) || current instanceof Admin);
        deleteButton.setVisible(canDelete);
        deleteButton.setManaged(canDelete);
        deleteButton.setOnAction(e -> {
            if (!DialogUtils.confirm("Delete Comment", "Delete this comment?", "This action cannot be undone.")) {
                return;
            }
            try {
                if (parent == null) {
                    article.removeComment(comment);
                } else {
                    parent.removeComment(comment);
                }
                articleRepository.save();
                renderArticle();
                ToastManager.success("Comment deleted");
            } catch (UnauthorizedActionException ex) {
                ToastManager.error(ex.getMessage());
            }
        });

        HBox actions = new HBox(10, replyButton, deleteButton);
        box.getChildren().addAll(author, content, meta, actions);

        for (Comment reply : comment.getReplies()) {
            box.getChildren().add(buildCommentNode(reply, depth + 1, comment));
        }
        return box;
    }

    @FXML
    public void onLikeClicked() {
        try {
            boolean nowLiked = article.toggleLike();
            articleRepository.save();
            updateLikeButton();
            likeCountLabel.setText(article.getLikes() + " likes");
            ToastManager.success(nowLiked ? "Liked!" : "Unliked");
        } catch (UnauthorizedActionException e) {
            ToastManager.error(e.getMessage());
        }
    }

    @FXML
    public void onReportClicked() {
        if (article.report()) {
            articleRepository.save();
            updateReportButton();
            ToastManager.success("Article reported. Thank you.");
        } else {
            ToastManager.info("You already reported this article.");
        }
    }

    @FXML
    public void onBookmarkClicked() {
        try {
            User current = Session.getCurrentUser();
            if (current == null) {
                ToastManager.error("You must be logged in to bookmark.");
                return;
            }
            User repoUser = userRepository.findUserByUsername(current.getUsername());
            repoUser.toggleBookmark(article.getId());
            Session.login(repoUser);
            userRepository.save();
            updateBookmarkButton();
            ToastManager.success(repoUser.isBookmarked(article.getId()) ? "Bookmarked" : "Unbookmarked");
        } catch (UnauthorizedActionException | UserNotFoundException e) {
            ToastManager.error(e.getMessage());
        } catch (Exception e) {
            ToastManager.error("Failed to toggle bookmark: " + e.getMessage());
        }
    }

    @FXML
    public void onReplyClicked(Comment parentComment) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Reply to " + parentComment.getAuthor().getDisplayName());
        dialog.setContentText("Your reply:");
        dialog.showAndWait().ifPresent(replyText -> {
            if (replyText.isBlank()) {
                ToastManager.error("Reply cannot be empty");
                return;
            }
            try {
                Comment reply = new Comment(Session.getCurrentUser(), replyText);
                parentComment.addComment(reply);
                articleRepository.save();
                renderArticle();
                ToastManager.success("Reply posted");
            } catch (UnauthorizedActionException e) {
                ToastManager.error(e.getMessage());
            }
        });
    }

    @FXML
    public void onPostCommentClicked() {
        String text = newCommentField.getText();
        if (text == null || text.isBlank()) {
            ToastManager.error("Comment cannot be empty");
            return;
        }
        try {
            Comment comment = new Comment(Session.getCurrentUser(), text);
            article.addComment(comment);
            articleRepository.save();
            newCommentField.clear();
            renderArticle();
            ToastManager.success("Comment posted");
        } catch (UnauthorizedActionException e) {
            ToastManager.error(e.getMessage());
        }
    }

    @FXML
    public void onEditClicked() {
        TextInputDialog titleDialog = new TextInputDialog(article.getTitle());
        titleDialog.setHeaderText("Edit Title");
        titleDialog.setContentText("Title:");
        titleDialog.showAndWait().ifPresent(newTitle -> {
            if (newTitle.isBlank()) {
                ToastManager.error("Title cannot be empty");
                return;
            }
            TextInputDialog contentDialog = new TextInputDialog(article.getContent());
            contentDialog.setHeaderText("Edit Content");
            contentDialog.setContentText("Content:");
            contentDialog.showAndWait().ifPresent(newContent -> {
                if (newContent.isBlank()) {
                    ToastManager.error("Content cannot be empty");
                    return;
                }
                try {
                    article.update(newTitle, newContent, article.getCategory());
                    articleRepository.save();
                    renderArticle();
                    ToastManager.success("Article updated");
                } catch (UnauthorizedActionException e) {
                    ToastManager.error(e.getMessage());
                }
            });
        });
    }

    @FXML
    public void onDeleteClicked() {
        if (!DialogUtils.confirm("Delete Article", "Delete this article?", "This action cannot be undone.")) {
            return;
        }
        try {
            moderationService.deleteArticle(article);
            ToastManager.success("Article deleted");
            SceneManager.switchTo("feed");
        } catch (UnauthorizedActionException e) {
            ToastManager.error(e.getMessage());
        }
    }

    @FXML
    public void onChangeTypeClicked() {
        String currentType = article.getTypeName();
        String newType = currentType.equals("Breaking News") ? "Editorial" : "Breaking News";
        Article newArticle = article.convertToType(newType);
        articleRepository.deleteArticle(article);
        articleRepository.addArticle(newArticle);
        articleRepository.save();
        article = newArticle;
        renderArticle();
        ToastManager.success("Article type changed to " + newType);
    }

    @FXML
    public void onDarkModeToggleClicked() {
        ThemeManager.toggleTheme(darkModeToggle.getScene());
    }

    @FXML
    public void onBackClicked() {
        AppState.setSelectedArticle(null);
        SceneManager.switchTo("feed");
    }
}