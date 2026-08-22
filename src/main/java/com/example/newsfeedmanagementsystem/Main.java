package com.example.newsfeedmanagementsystem;

import com.example.newsfeedmanagementsystem.exception.DuplicateUserException;
import com.example.newsfeedmanagementsystem.model.BreakingNews;
import com.example.newsfeedmanagementsystem.model.Journalist;
import com.example.newsfeedmanagementsystem.model.RegularUser;
import com.example.newsfeedmanagementsystem.repository.ArticleRepository;
import com.example.newsfeedmanagementsystem.repository.UserRepository;

public class Main {
    public static void main(String[] args) throws DuplicateUserException {
        UserRepository userRepository = new UserRepository();
        ArticleRepository articleRepository = new ArticleRepository();

        RegularUser user1 = new RegularUser("Habib" , "123" , "habiba");
        Journalist user2 = new Journalist("Labib" , "123" , "labiba");

        userRepository.addUser(user1);
        userRepository.addUser(user2);

        BreakingNews news1 = new BreakingNews("Lobotomy" , "Lorem Ipsum Dolor" ,user2, "Business");
        articleRepository.addArticle(news1);

        System.out.println("Before saving");
        System.out.println(user1);
        System.out.println(user2);
        System.out.println(news1);

        userRepository.save();
        articleRepository.save();

        UserRepository userRepository2 = new UserRepository();
        ArticleRepository articleRepository2 = new ArticleRepository();

        userRepository2.load();
        articleRepository2.load();

        System.out.println("After saving");

        userRepository2.getAllUsers().forEach(System.out::println);
        articleRepository2.getAllArticles().forEach(System.out::println);
    }
}
