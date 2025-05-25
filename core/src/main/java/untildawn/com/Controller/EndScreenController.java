package untildawn.com.Controller;

import untildawn.com.Main;
import untildawn.com.Model.Player;
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
