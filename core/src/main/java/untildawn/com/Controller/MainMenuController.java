package untildawn.com.Controller;

import untildawn.com.Main;
import untildawn.com.Model.User;
import untildawn.com.View.*;

public class MainMenuController {
    private final Main game;
    private User user;

    public MainMenuController(Main game) {
        this.game = game;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public int getUserScore() {
        // Since User class doesn't have a score field yet,
        // return a placeholder score based on username length
        if (user != null && !user.isGuest()) {
            // This is temporary until User has proper score tracking
            return user.getUsername().length() * 100;
        }
        return 0;
    }

    public void logout() {
        // Return to pre-menu screen
        game.setScreen(new PreMenuView(game));
    }

    public void resumeGame() {
        // Implementation will be added later
    }

    public void openPreGame() {
        game.setScreen(new PreGameMenuView(game, user));
    }

    public void openProfile() {
        if (user != null) {
            game.setScreen(new ProfileMenuView(game, user));
        }
    }

    public void openScoreboard() {
        // Implementation will be added later
    }

    public void openTalents() {
        game.setScreen(new TalentMenuView(game, user));
    }
    public void openSettings() {
        game.setScreen(new SettingsMenuView(game, user));
    }
}
