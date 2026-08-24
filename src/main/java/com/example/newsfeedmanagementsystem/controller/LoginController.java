package com.example.newsfeedmanagementsystem.controller;

import com.example.newsfeedmanagementsystem.exception.DuplicateUserException;
import com.example.newsfeedmanagementsystem.exception.InvalidCredentialsException;
import com.example.newsfeedmanagementsystem.exception.UnauthorizedActionException;
import com.example.newsfeedmanagementsystem.exception.UserNotFoundException;
import com.example.newsfeedmanagementsystem.model.User;
import com.example.newsfeedmanagementsystem.repository.UserRepository;
import com.example.newsfeedmanagementsystem.service.AuthService;
import com.example.newsfeedmanagementsystem.util.SceneManager;
import com.example.newsfeedmanagementsystem.util.ThemeManager;
import com.example.newsfeedmanagementsystem.util.ToastManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private VBox displayNameContainer;
    @FXML
    private VBox roleContainer;
    @FXML
    private TextField displayNameField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Button submitButton;
    @FXML
    private Hyperlink toggleModeLink;

    private boolean isRegisterMode = false;
    private AuthService authService;
    private UserRepository userRepository;

    @FXML
    public void initialize() {
        this.userRepository = new UserRepository();
        userRepository.load();
        this.authService = new AuthService(userRepository);
        roleComboBox.getItems().addAll("REGULAR", "JOURNALIST");
    }

    @FXML
    public void onToggleModeClicked() {
        isRegisterMode = !isRegisterMode;
        updateUIForMode();
    }

    private void updateUIForMode() {
        if (isRegisterMode) {
            displayNameContainer.setVisible(true);
            displayNameContainer.setManaged(true);
            roleContainer.setVisible(true);
            roleContainer.setManaged(true);
            submitButton.setText("Register!");
            toggleModeLink.setText("Already have an account? Login");
        } else {
            displayNameContainer.setVisible(false);
            displayNameContainer.setManaged(false);
            roleContainer.setVisible(false);
            roleContainer.setManaged(false);

            submitButton.setText("Login!");
            toggleModeLink.setText("Don't have an account? Register");
        }
    }

    @FXML
    public void onSubmitClicked() {
        if (isRegisterMode) {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String displayName = displayNameField.getText();
            String role = (roleComboBox.getSelectionModel().getSelectedItem() == null) ?
                    null :
                    roleComboBox.getSelectionModel().getSelectedItem().toString();

            if (username.isEmpty() || password.isEmpty() || displayName.isEmpty() || role == null) {
                ToastManager.error("Please fill all the fields");
                return;
            }
            try {
                authService.register(username, password, displayName, role);
                userRepository.save();
                ToastManager.success("Account Crated! Please Login");
                isRegisterMode = false;
                updateUIForMode();
                passwordField.clear();
            } catch (DuplicateUserException e) {
                ToastManager.error(e.getMessage());
            }
        } else {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                ToastManager.error("Please fill all the fields");
                return;
            }

            try {
                User user = authService.login(username, password);
                ToastManager.success("Welcome back, " + user.getDisplayName() + "!");
                SceneManager.switchTo("feed");
            } catch (UserNotFoundException | InvalidCredentialsException | UnauthorizedActionException e) {
                ToastManager.error(e.getMessage());
            }
        }
    }

    @FXML
    public void onDarkModeToggleClicked() {
        ThemeManager.toggleTheme(usernameField.getScene());
    }
}
