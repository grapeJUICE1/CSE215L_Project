package com.example.newsfeedmanagementsystem.repository;

import com.example.newsfeedmanagementsystem.model.Article;
import com.example.newsfeedmanagementsystem.model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ArticleRepository {
    private List<Article> articles = new ArrayList<>();
    private static final String FILE_PATH = "articles.dat";

    public void addArticle(Article article) {
        this.articles.add(article);
    }

    public List<Article> getAllArticles() {
        return this.articles;
    }

    public void deleteArticle(Article article) {
        this.articles.remove(article);
    }

    public List<Article> getArticlesByUser(User user) {
        return articles.stream()
                .filter(a->a.getAuthor().equals(user))
                .collect(Collectors.toList());
    }

    public Article findById(String id) {
        return articles.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void save(){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))){
            oos.writeObject(articles);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void load(){
        File file = new File(FILE_PATH);
        if(!file.exists()) return;

        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))){
            articles = (List<Article>) ois.readObject();
        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

}
