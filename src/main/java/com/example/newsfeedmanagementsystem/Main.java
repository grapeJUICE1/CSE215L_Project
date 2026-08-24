package com.example.newsfeedmanagementsystem;

import com.example.newsfeedmanagementsystem.model.*;
import com.example.newsfeedmanagementsystem.repository.*;
import com.example.newsfeedmanagementsystem.service.*;
import com.example.newsfeedmanagementsystem.util.Session;

public class Main {
    public static void main(String[] args) throws Exception {
        UserRepository userRepo = new UserRepository();
        ArticleRepository articleRepo = new ArticleRepository();

        AuthService authService = new AuthService(userRepo);
        FeedService feedService = new FeedService();
        ModerationService modService = new ModerationService(userRepo, articleRepo);

        User journalist = authService.register("labib", "pass123", "Labib", "Journalist");
        User reader = authService.register("habib", "pass123", "Habib", "USER");
        User admin = authService.register("root", "adminpass", "Root Admin", "ADMIN");

        System.out.println("--- Registered ---");
        System.out.println(journalist);
        System.out.println(reader);
        System.out.println(admin);

        User loggedInJournalist = authService.login("labib", "pass123");
        Article article = new BreakingNews("City Floods", "Heavy rain causes flooding downtown", (Journalist) loggedInJournalist, "Weather");
        articleRepo.addArticle(article);

        System.out.println("--- Published ---");
        System.out.println(article);

        User loggedInReader = authService.login("habib", "pass123");
        article.like();
        article.addComment(new Comment(loggedInReader, "Stay safe everyone!"));

        System.out.println("--- After interaction ---");
        System.out.println(article);
        article.getComments().forEach(System.out::println);

        var feed = feedService.getFeed(articleRepo.getAllArticles(), null, "Weather", null, "recency", 0, 10);
        System.out.println("--- Feed (Weather category) ---");
        feed.forEach(System.out::println);

        User loggedInAdmin = authService.login("root", "adminpass");
        modService.banUser(reader);
        System.out.println("--- After ban ---");
        System.out.println(reader);

        try {
            authService.login("habib", "pass123");
        } catch (Exception e) {
            System.out.println("Expected failure: " + e.getMessage());
        }

        userRepo.save();
        articleRepo.save();

        UserRepository userRepo2 = new UserRepository();
        userRepo2.load();
        System.out.println("--- Reloaded users ---");
        userRepo2.getAllUsers().forEach(System.out::println);
    }
}