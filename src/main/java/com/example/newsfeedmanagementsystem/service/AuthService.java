package com.example.newsfeedmanagementsystem.service;

import com.example.newsfeedmanagementsystem.exception.DuplicateUserException;
import com.example.newsfeedmanagementsystem.exception.InvalidCredentialsException;
import com.example.newsfeedmanagementsystem.exception.UnauthorizedActionException;
import com.example.newsfeedmanagementsystem.exception.UserNotFoundException;
import com.example.newsfeedmanagementsystem.model.Admin;
import com.example.newsfeedmanagementsystem.model.Journalist;
import com.example.newsfeedmanagementsystem.model.RegularUser;
import com.example.newsfeedmanagementsystem.model.User;
import com.example.newsfeedmanagementsystem.util.PasswordHasher;
import com.example.newsfeedmanagementsystem.repository.UserRepository;
import com.example.newsfeedmanagementsystem.util.Session;

public class AuthService {
    UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String password, String displayName, String role) throws DuplicateUserException {
        String hashedPassword = PasswordHasher.hashPassword(password);
        User newUser;

        switch (role) {
            case "ADMIN":
                newUser = new Admin(username, hashedPassword, displayName);
                break;

            case "USER":
                newUser = new RegularUser(username, hashedPassword, displayName);
                break;

            case "Journalist":
                newUser = new Journalist(username, hashedPassword, displayName);
                break;

            default:
                newUser = new RegularUser(username, hashedPassword, displayName);
                break;
        }

        userRepository.addUser(newUser);

        return newUser;
    }

    public User login(String username, String password) throws UserNotFoundException, InvalidCredentialsException,UnauthorizedActionException
    {
        User user = userRepository.findUserByUsername(username);
        if(user.isBanned()){
            throw new UnauthorizedActionException("User is banned");
        }
        boolean valid = PasswordHasher.checkPassword(password, user.getPasswordHash());
        if(!valid)
            throw new InvalidCredentialsException("Invalid username or password");

        Session.login(user);
        return user;
    }

    public void changePassword(String oldPassword,String newPassword) throws  InvalidCredentialsException, UnauthorizedActionException {
        User currentUser = Session.getCurrentUser();
        boolean valid = PasswordHasher.checkPassword(oldPassword, currentUser.getPasswordHash());
        if(!valid)
            throw new InvalidCredentialsException("Invalid username or password");

        String newHash = PasswordHasher.hashPassword(newPassword);
        currentUser.setPasswordHash(newHash);
        try {
            User repoUser = userRepository.findUserByUsername(currentUser.getUsername());
            repoUser.setPasswordHash(newHash);
        } catch (UserNotFoundException e) {
        }

        userRepository.save();    }
}
