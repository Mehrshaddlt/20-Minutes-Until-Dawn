package untildawn.com.Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import untildawn.com.Main;
import untildawn.com.Model.User;
import untildawn.com.View.MainMenuView;
import untildawn.com.View.PreMenuView;

public class RegisterMenuController {
    private Main game;
    private String lastError;

    public RegisterMenuController(Main game) {
        this.game = game;
    }
    public boolean register(String username, String password, String securityQuestion, String securityAnswer) {
        if (User.checkUserExists(username)) {
            lastError = "Username already exists";
            return false;
        }
        if (!validateRegistrationData(username, password)) {
            return false;
        }
        if (securityQuestion.isEmpty()) {
            lastError = "Security question cannot be empty";
            return false;
        }
        if (securityAnswer.isEmpty()) {
            lastError = "Security answer cannot be empty";
            return false;
        }
        User newUser = new User(username, password, securityQuestion, securityAnswer);
        String avatarPath = getRandomAvatar();
        newUser.setAvatarPath(avatarPath);
        boolean success = User.saveUser(newUser);
        if (!success) {
            lastError = "Failed to save user";
            return false;
        }
        game.setScreen(new MainMenuView(game, newUser));
        return true;
    }
    public boolean validateRegistrationData(String username, String password) {
        if (username.isEmpty()) {
            lastError = "Username cannot be empty";
            return false;
        }
        if (username.length() < 3) {
            lastError = "Username must be at least 3 characters long";
            return false;
        }
        if (password.isEmpty()) {
            lastError = "Password cannot be empty";
            return false;
        }
        if (password.length() < 8) {
            lastError = "Password must be at least 8 characters long";
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            lastError = "Password must contain at least one capital letter";
            return false;
        }
        if (!password.matches(".*[0-9].*")) {
            lastError = "Password must contain at least one number";
            return false;
        }
        if (!password.matches(".*[_()*&%$#@].*")) {
            lastError = "Password must contain at least one special character: _()*&%$#@";
            return false;
        }
        return true;
    }
    public boolean isUsernameAvailable(String username) {
        if (User.checkUserExists(username)) {
            lastError = "Username already exists";
            return false;
        }
        return true;
    }
    public void playAsGuest() {
        User guestUser = new User();
        guestUser.setUsername("Guest" + System.currentTimeMillis());
        game.setScreen(new MainMenuView(game, guestUser));
    }
    public void goBack() {
        game.setScreen(new PreMenuView(game));
    }

    public String getLastError() {
        return lastError;
    }
    private String getRandomAvatar() {
        FileHandle dirHandle = Gdx.files.internal("Avatars");
        FileHandle[] files = dirHandle.list(".png");
        if (files.length == 0) {
            return "Avatars/T_Abby_Portrait.png";
        }
        int randomIndex = (int) (Math.random() * files.length);
        return "Avatars/" + files[randomIndex].name();
    }
}
