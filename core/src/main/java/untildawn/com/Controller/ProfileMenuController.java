package untildawn.com.Controller;

import untildawn.com.Main;
import untildawn.com.Model.User;
import untildawn.com.View.MainMenuView;
import untildawn.com.View.PreMenuView;

public class ProfileMenuController {
    private Main game;
    private User currentUser;

    public ProfileMenuController(Main game, User currentUser) {
        this.game = game;
        this.currentUser = currentUser;
    }

    public boolean updateUsername(String newUsername) {
        // Validate username
        if (newUsername.length() < 3) {
            return false;
        }

        // Check if username exists (except current user)
        if (User.isUsernameTaken(newUsername, currentUser)) {
            return false;
        }

        currentUser.setUsername(newUsername);
        return true;
    }

    public boolean updatePassword(String newPassword) {
        // Password validation
        if (newPassword.isEmpty()) {
            return false;
        }

        if (newPassword.length() < 8) {
            return false;
        }

        if (!newPassword.matches(".*[A-Z].*")) {
            return false;
        }

        if (!newPassword.matches(".*[0-9].*")) {
            return false;
        }

        if (!newPassword.matches(".*[_()*&%$#@].*")) {
            return false;
        }

        currentUser.setPassword(newPassword);
        return true;
    }

    public void setAvatar(String avatarName) {
        // Set avatar path directly using the path format
        currentUser.setAvatarPath("Avatars/T_" + avatarName + "_Portrait.png");
    }

    public void deleteAccount() {
        // Remove the user from the users list instead of using a non-existent deleteUser method
        User.removeUser(currentUser);
        game.setScreen(new PreMenuView(game));
    }

    public void backToMainMenu() {
        game.setScreen(new MainMenuView(game, currentUser));
    }
}
