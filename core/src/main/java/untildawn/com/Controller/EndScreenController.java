package untildawn.com.Controller;

import untildawn.com.Main;
import untildawn.com.Model.Player;
import untildawn.com.Model.SaveManager;
import untildawn.com.Model.User;
import untildawn.com.View.EndScreenView;
import untildawn.com.View.PreMenuView;

public class EndScreenController {
    private final Main game;
    private final Player player;
    private final GameController gameController;
    private final int killCount;
    private final int timeInSeconds;
    private final boolean gameWon;
    private User currentUser;

    public EndScreenController(Main game, GameController gameController, boolean gameWon, int timeInSeconds, User currentUser) {
        this.game = game;
        this.gameController = gameController;
        this.player = gameController.getPlayer();
        this.killCount = player.getKillCount();
        this.timeInSeconds = timeInSeconds;
        this.gameWon = gameWon;
        this.currentUser = currentUser;

        saveUserProgress();
    }

    // In EndScreenController.java
    private void saveUserProgress() {
        if (currentUser != null && !currentUser.isGuest()) {
            // Calculate score based on kills and time
            int gameScore = killCount * timeInSeconds;

            System.out.println("Game Score calculation:");
            System.out.println("Kills: " + killCount + " × Time: " + timeInSeconds);
            System.out.println("Final Game Score: " + gameScore);

            // Use the existing updateGameStats method instead of manually updating fields
            // This method correctly adds to the total score and handles all other stat updates
            currentUser.updateGameStats(gameScore, killCount, gameWon);

            System.out.println("Updated total score: " + currentUser.getTotalScore());
            System.out.println("High score: " + currentUser.getHighScore());

            // Verify the save worked (updateGameStats already saves the user, but this check is useful)
            User verifyUser = SaveManager.loadUser(currentUser.getUsername());
            if (verifyUser != null) {
                System.out.println("Verified saved data - Total score: " +
                    verifyUser.getTotalScore() + ", High score: " + verifyUser.getHighScore());
            }
        }
    }
    public void returnToMainMenu() {
        game.setScreen(new PreMenuView(game));
    }

    public int getKillCount() {
        return killCount;
    }

    public int getTimeInSeconds() {
        return timeInSeconds;
    }

    public boolean isGameWon() {
        return gameWon;
    }
    public User getCurrentUser() {
        return currentUser;
    }
    public String getEndGameMessage() {
        return gameWon ? "You Won!" : "You Lost!";
    }
    public int getTotalGameTimeInSeconds() {
        // Use the initialGameTimeInSeconds from GameController
        return gameController.getTotalGameTimeInSeconds();
    }
    public Player getPlayer() {
        return player;
    }

}
