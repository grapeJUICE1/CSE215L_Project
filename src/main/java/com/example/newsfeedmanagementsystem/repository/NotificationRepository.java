package com.example.newsfeedmanagementsystem.repository;

import com.example.newsfeedmanagementsystem.model.Notification;
import com.example.newsfeedmanagementsystem.model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationRepository {
    private List<Notification> notifications = new ArrayList<>();
    private static final String FILE_PATH = "notifications.dat";

    public void addNotification(Notification notification) {
        this.notifications.add(notification);
    }

    public List<Notification> getAllNotifications() {
        return this.notifications;
    }

    public void deleteNotification(Notification notifications) {
        this.notifications.remove(notifications);
    }

    public List<Notification> getNotificationsByUser(User user) {
       return notifications.stream()
               .filter(n -> n.getRecipient().equals(user))
               .collect(Collectors.toList());
    }

    public void save(){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))){
            oos.writeObject(notifications);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void load(){
        File file = new File(FILE_PATH);
        if(!file.exists()) return;

        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))){
            notifications = (List<Notification>) ois.readObject();
        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

}
