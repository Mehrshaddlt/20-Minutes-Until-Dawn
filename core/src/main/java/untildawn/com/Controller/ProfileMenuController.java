package untildawn.com.Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import untildawn.com.Main;
import untildawn.com.Model.SaveManager;
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
        if (newUsername.length() < 3) return false;
        if (User.isUsernameTaken(newUsername, currentUser)) return false;
        String oldUsername = currentUser.getUsername();
        currentUser.setUsername(newUsername);
        SaveManager.saveUser(currentUser);
        if (!oldUsername.equals(newUsername)) {
            FileHandle oldFile = Gdx.files.local("users/" + oldUsername + ".json");
            if (oldFile.exists()) oldFile.delete();
        }
        return true;
    }

    public boolean updatePassword(String newPassword) {
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
        SaveManager.saveUser(currentUser);
        return true;
    }

    public void setCustomAvatar(String filePath) {
        currentUser.setAvatarPath(filePath);
        SaveManager.saveUser(currentUser);
    }
    public void setAvatar(String avatarName) {
        currentUser.setAvatarPath("Avatars/T_" + avatarName + "_Portrait.png");
        SaveManager.saveUser(currentUser);
    }
    public void deleteAccount() {
        User.removeUser(currentUser);
        FileHandle file = Gdx.files.local("users/" + currentUser.getUsername() + ".json");
        if (file.exists()) file.delete();
        game.setScreen(new PreMenuView(game));
    }
    public void backToMainMenu() {
        game.setScreen(new MainMenuView(game, currentUser));
    }
}
