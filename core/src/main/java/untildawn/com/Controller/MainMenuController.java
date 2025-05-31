package untildawn.com.Controller;

import untildawn.com.Main;
import untildawn.com.Model.*;
import untildawn.com.View.*;

public class MainMenuController {
    private final Main game;
    private User user;

    public MainMenuController(Main game) {
        this.game = game;
    }
    public void setUser(User user) {
        this.user = user;
        if (user != null && !user.isGuest()) {
            User updatedUser = SaveManager.getUserByUsername(user.getUsername());
            if (updatedUser != null) {
                this.user = updatedUser;
            }
        }
    }

    public User getUser() {
        return user;
    }
    public int getUserScore() {
        if (user != null) {
            return user.getTotalScore();
        }
        return 0;
    }
    public void logout() {
        // Return to pre-menu screen
        game.setScreen(new PreMenuView(game));
    }
    public void resumeGame() {
        if (SaveGameManager.hasLastGame()) {
            WeaponsManager weaponsManager = new WeaponsManager();
            EnemiesManager enemiesManager = new EnemiesManager(2000, 2000, weaponsManager);
            GameController gameController = new GameController(
                user,
                weaponsManager,
                enemiesManager,
                2,
                new SettingsMenuController(game, user)
            );
            SaveGameManager.loadLastGame(gameController);
            game.setScreen(new GameView(game, user, gameController.getTotalGameTimeInSeconds()));
        }
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
        game.setScreen(new LeaderboardMenuView(game, user));
    }
    public void openTalents() {
        game.setScreen(new TalentMenuView(game, user));
    }
    public void openSettings() {
        game.setScreen(new SettingsMenuView(game, user));
    }
}
