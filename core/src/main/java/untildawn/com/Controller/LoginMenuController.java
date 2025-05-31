package untildawn.com.Controller;

import untildawn.com.Main;
import untildawn.com.Model.User;
import untildawn.com.View.MainMenuView;
import untildawn.com.View.PreMenuView;
import untildawn.com.View.RegisterMenuView;

public class LoginMenuController {
    private Main game;
    private String lastError;
    public LoginMenuController(Main game) {
        this.game = game;
    }

    public boolean login(String username, String password) {
        if (username.isEmpty()) {
            lastError = "Username cannot be empty";
            return false;
        }
        if (password.isEmpty()) {
            lastError = "Password cannot be empty";
            return false;
        }
        if (User.checkUserExists(username)) {
            User user = User.getUserByUsername(username);
            if (user.getPassword().equals(password)) {
                // Login successful, navigate to main menu
                game.setScreen(new MainMenuView(game, user));
                return true;
            }
            else {
                lastError = "Incorrect password";
                return false;
            }
        }
        else {
            lastError = "User not found";
            return false;
        }
    }
    public boolean forgotPassword(String username) {
        if (username.isEmpty()) {
            lastError = "Username cannot be empty";
            return false;
        }
        if (!User.checkUserExists(username)) {
            lastError = "User not found";
            return false;
        }
        return true;
    }
    public boolean verifySecurityAnswer(String username, String answer) {
        User user = User.getUserByUsername(username);
        if (answer.isEmpty()) {
            lastError = "Answer cannot be empty";
            return false;
        }
        if (!user.getSecurityAnswer().equals(answer)) {
            lastError = "Incorrect answer";
            return false;
        }
        return true;
    }
    public boolean resetPassword(String username, String newPassword) {
        User user = User.getUserByUsername(username);
        if (newPassword.isEmpty()) {
            lastError = "Password cannot be empty";
            return false;
        }
        if (newPassword.length() < 8) {
            lastError = "Password must be at least 8 characters long";
            return false;
        }
        if (!newPassword.matches(".*[A-Z].*")) {
            lastError = "Password must contain at least one capital letter";
            return false;
        }
        if (!newPassword.matches(".*[0-9].*")) {
            lastError = "Password must contain at least one number";
            return false;
        }
        if (!newPassword.matches(".*[_()*&%$#@].*")) {
            lastError = "Password must contain at least one special character: _()*&%$#@";
            return false;
        }
        user.setPassword(newPassword);
        return true;
    }
    public void goToRegister() {
        game.setScreen(new RegisterMenuView(game));
    }
    public void goBack() {
        game.setScreen(new PreMenuView(game));
    }
    public String getLastError() {
        return lastError;
    }
}
