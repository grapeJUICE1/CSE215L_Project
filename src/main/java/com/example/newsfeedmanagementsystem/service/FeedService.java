package com.example.newsfeedmanagementsystem.service;

import com.example.newsfeedmanagementsystem.model.Article;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FeedService {
    private List<Article> searchArticles(String searchText, List<Article> articles) {
        return articles.stream()
                .filter(article -> article.getTitle().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<Article> filterByCategory(String category, List<Article> articles) {
        return articles.stream()
                .filter(article -> article.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    private List<Article> filterByAuthor(String username, List<Article> articles) {
        return articles.stream()
                .filter(article -> article.getAuthor().getUsername().equals(username))
                .collect(Collectors.toList());
    }

    private List<Article> sortByRecency(List<Article> articles) {
        List<Article> articlesCopy = new ArrayList<>(articles);
        articlesCopy.sort(Comparator.comparing(Article::getPublishedAt).reversed());

        return articlesCopy;
    }


    private List<Article> sortByEngagement(List<Article> articles) {
        List<Article> articlesCopy = new ArrayList<>(articles);
        articlesCopy.sort(
                Comparator.comparingInt((Article a) -> a.getLikes() + a.getComments().size())
                        .reversed());

        return articlesCopy;
    }

    private List<Article> paginate(List<Article> articles, int pageNumber, int pageSize) {
        int fromIndex = pageNumber  * pageSize;
        if(fromIndex > articles.size())
            return Collections.emptyList();
        int toIndex = Math.min(fromIndex + pageSize,articles.size());
        return articles.subList(fromIndex, toIndex);
    }
    public List<String> getAllCategories(List<Article> articles) {
        return articles.stream()
                .map(Article::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Article> getFeed(List<Article> articles , String searchQuery,String category,String username,String sortMode,int page,int pageSize) {
        if(searchQuery != null && !searchQuery.isEmpty())
            articles = searchArticles(searchQuery,articles);
        if(category != null && !category.isEmpty())
            articles = filterByCategory(category,articles);
        if(username != null && !username.isEmpty())
            articles = filterByAuthor(username,articles);
        if(sortMode != null && sortMode.equals("recency"))
            articles = sortByRecency(articles);
        if(sortMode != null && sortMode.equals("engagement"))
            articles = sortByEngagement(articles);

        return paginate(articles,page,pageSize);
    }
}
