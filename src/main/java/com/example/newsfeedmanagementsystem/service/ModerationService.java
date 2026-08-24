package com.example.newsfeedmanagementsystem.service;

import com.example.newsfeedmanagementsystem.exception.DuplicateUserException;
import com.example.newsfeedmanagementsystem.exception.UnauthorizedActionException;
import com.example.newsfeedmanagementsystem.exception.UserNotFoundException;
import com.example.newsfeedmanagementsystem.model.*;
import com.example.newsfeedmanagementsystem.repository.ArticleRepository;
import com.example.newsfeedmanagementsystem.repository.UserRepository;
import com.example.newsfeedmanagementsystem.util.Session;

import java.util.List;
import java.util.stream.Collectors;

public class ModerationService {
    UserRepository userRepository;
    ArticleRepository articleRepository;

    public ModerationService(UserRepository userRepository, ArticleRepository articleRepository) {
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
    }

    private void verifyAdmin() throws UnauthorizedActionException {
        User current = Session.getCurrentUser();
        if (!(current instanceof Admin)) {
            throw new UnauthorizedActionException("Only administrators can perform this action.");
        }
    }

    public void deleteArticle(Article article) throws UnauthorizedActionException {
        User current = Session.getCurrentUser();
        if (current == null) throw new UnauthorizedActionException("Not logged in");
        boolean isAdmin = current instanceof Admin;
        boolean isAuthor = current.equals(article.getAuthor());
        if (!isAdmin && !isAuthor) {
            throw new UnauthorizedActionException("Only the author or an admin can delete this article.");
        }
        articleRepository.deleteArticle(article);
        articleRepository.save();
    }

    public void promoteToJournalist(String username) throws UnauthorizedActionException, UserNotFoundException, DuplicateUserException {
        verifyAdmin();
        User user = userRepository.findUserByUsername(username);
        if (user instanceof Journalist || user instanceof Admin) {
            throw new UnauthorizedActionException("User is already a Journalist or Admin.");
        }
        Journalist newJournalist = new Journalist(user.getUsername(), user.getPasswordHash(), user.getDisplayName());
        if (user.isBanned()) {
            newJournalist.setBanned(true);
        }
        userRepository.removeUser(user);
        userRepository.addUser(newJournalist);
        userRepository.save();
    }

    public void toggleBanStatus(String username) throws UnauthorizedActionException, UserNotFoundException {
        verifyAdmin();
        User user = userRepository.findUserByUsername(username);
        if (user instanceof Admin)
            throw new UnauthorizedActionException("Cannot ban another administrator.");
        user.setBanned(!user.isBanned());
        userRepository.save();
    }

    public List<Article> getReportedArticles() throws UnauthorizedActionException {
        verifyAdmin();
        return articleRepository.getAllArticles().stream()
                .filter(article -> article.getReportCount() > 0)
                .collect(Collectors.toList());
    }
}