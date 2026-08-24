package com.example.newsfeedmanagementsystem.controller;

import com.example.newsfeedmanagementsystem.exception.UnauthorizedActionException;
import com.example.newsfeedmanagementsystem.model.Admin;
import com.example.newsfeedmanagementsystem.model.Article;
import com.example.newsfeedmanagementsystem.model.Comment;
import com.example.newsfeedmanagementsystem.repository.ArticleRepository;
import com.example.newsfeedmanagementsystem.repository.UserRepository;
import com.example.newsfeedmanagementsystem.service.ModerationService;
import com.example.newsfeedmanagementsystem.util.AppState;
import com.example.newsfeedmanagementsystem.util.SceneManager;
import com.example.newsfeedmanagementsystem.util.Session;
import com.example.newsfeedmanagementsystem.util.ToastManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ArticleDetailController {

    @FXML private Label categoryTag;
    @FXML private Label titleLabel;
    @FXML private Label metaLabel;
    @FXML private Label contentLabel;
    @FXML private Label likeCountLabel;
    @FXML private TextArea newCommentField;
    @FXML private VBox commentsContainer;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

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
        if(article == null) {
            ToastManager.error("Article not found");
            SceneManager.switchTo("feed");
            return;
        }

        boolean isAuthor = Session.getCurrentUser() != null && Session.getCurrentUser().equals(article.getAuthor());
        editButton.setVisible(isAuthor);
        editButton.setManaged(isAuthor);

        boolean isAdmin = Session.getCurrentUser() instanceof Admin;
        deleteButton.setVisible(isAdmin);
        deleteButton.setManaged(isAdmin);

        renderArticle();
    }

    private void renderArticle() {
        categoryTag.setText(article.getCategory());
        titleLabel.setText(article.getTitle());
        metaLabel.setText("By " + article.getAuthor().getDisplayName() + " • " + article.getPublishedAt());
        contentLabel.setText(article.getContent());
        likeCountLabel.setText(article.getLikes() + " likes");
        renderComments();
    }

    private void renderComments() {
        commentsContainer.getChildren().clear();
        for (Comment comment : article.getComments()) {
            commentsContainer.getChildren().add(buildCommentNode(comment, 0));
        }
    }

    private VBox buildCommentNode(Comment comment, int depth) {
        VBox box = new VBox(4);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(10, 10, 10, 10 + depth * 24));

        Label author = new Label(comment.getAuthor().getDisplayName());
        author.getStyleClass().add("field-label");

        Label content = new Label(comment.getContent());
        content.getStyleClass().add("article-body");
        content.setWrapText(true);

        Label meta = new Label(comment.getTimestamp().toString());
        meta.getStyleClass().add("article-meta");

        Button replyButton = new Button("Reply");
        replyButton.getStyleClass().add("nav-link");
        replyButton.setOnAction(e -> onReplyClicked(comment));

        HBox actions = new HBox(10, replyButton);

        box.getChildren().addAll(author, content, meta, actions);

        for (Comment reply : comment.getReplies()) {
            box.getChildren().add(buildCommentNode(reply, depth + 1));
        }

        return box;
    }

    private void onReplyClicked(Comment parentComment) {
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
    public void onLikeClicked() {
        try {
            article.like();
            articleRepository.save();
            renderArticle();
            ToastManager.success("Liked!");
        } catch (UnauthorizedActionException e) {
            ToastManager.error(e.getMessage());
        }
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
        try {
            moderationService.deleteArticle(article);
            ToastManager.success("Article deleted");
            SceneManager.switchTo("feed");
        } catch (UnauthorizedActionException e) {
            ToastManager.error(e.getMessage());
        }
    }

    @FXML
    public void onReportClicked() {
        article.report();
        articleRepository.save();
        ToastManager.success("Article reported. Thank you.");
    }

    @FXML
    public void onBackClicked() {
        SceneManager.switchTo("feed");
    }
}