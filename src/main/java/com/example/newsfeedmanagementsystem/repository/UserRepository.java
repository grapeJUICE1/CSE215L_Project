package com.example.newsfeedmanagementsystem.repository;

import com.example.newsfeedmanagementsystem.exception.DuplicateUserException;
import com.example.newsfeedmanagementsystem.exception.UserNotFoundException;
import com.example.newsfeedmanagementsystem.model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private Map<String, User> usersByUsername = new HashMap<>();
    private static final String FILE_PATH = "users.dat";

    public void addUser(User user) throws DuplicateUserException {
        if(usersByUsername.containsKey(user.getUsername())) {
            throw new DuplicateUserException("Username already exists: " + user.getUsername());
        }
        usersByUsername.put(user.getUsername(), user);
    }

    public void removeUser(User oldUser) throws UserNotFoundException {
        if(usersByUsername.containsKey(oldUser.getUsername())) {
            usersByUsername.remove(oldUser.getUsername());
        } else {
            throw new UserNotFoundException("User not found: " + oldUser.getUsername());
        }
    }

    public User findUserByUsername(String username) throws UserNotFoundException {
        User user = usersByUsername.get(username);
        if(user == null) {
            throw new UserNotFoundException("User not found: " + username);
        }
        return user;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(usersByUsername.values());

    }

    public void save(){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))){
            oos.writeObject(usersByUsername);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void load(){
        File file = new File(FILE_PATH);
        if(!file.exists()) return;

        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))){
            usersByUsername = (Map<String, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

}
