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

    public void deleteArticle( Article article) throws UnauthorizedActionException {
        User currentUser = Session.getCurrentUser();

        if(!(currentUser instanceof Admin))
            throw new UnauthorizedActionException("You are not allowed to perform this action");

        articleRepository.deleteArticle(article);
        articleRepository.save();
    }

    public void promoteToJournalist(RegularUser user) throws UnauthorizedActionException, UserNotFoundException, DuplicateUserException {
        User currentUser = Session.getCurrentUser();

        if(!(currentUser instanceof Admin))
            throw new UnauthorizedActionException("You are not allowed to perform this action");

        Journalist newJournalist = new Journalist(user.getUsername(),user.getPasswordHash(),user.getDisplayName());
        userRepository.removeUser(user);
        userRepository.addUser(newJournalist);
        userRepository.save();
    }

    public void banUser(User user) throws UnauthorizedActionException{
        User currentUser = Session.getCurrentUser();

        if(!(currentUser instanceof Admin))
            throw new UnauthorizedActionException("You are not allowed to perform this action");

       user.setBanned(true);
       userRepository.save();
    }

    public void unbanUser(User user) throws UnauthorizedActionException {
        User currentUser = Session.getCurrentUser();

        if(!(currentUser instanceof Admin))
            throw new UnauthorizedActionException("You are not allowed to perform this action");

        user.setBanned(false);
        userRepository.save();
    }

    public List<Article> getReportedArticles() throws UnauthorizedActionException {
        User currentUser = Session.getCurrentUser();

        if(!(currentUser instanceof Admin))
            throw new UnauthorizedActionException("You are not allowed to perform this action");

        return articleRepository.getAllArticles().stream()
                .filter( article -> article.getReportCount() > 0)
                .collect(Collectors.toList()) ;
    }

}
