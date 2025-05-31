package untildawn.com.Controller;

import com.badlogic.gdx.Gdx;
import untildawn.com.Main;
import untildawn.com.Model.Player;
import untildawn.com.View.GameView;
import untildawn.com.View.PreMenuView;

import java.util.HashMap;
import java.util.Map;

public class PauseMenuController {
    private boolean isPaused;
    private final GameController gameController;
    private final Main game;
    private boolean wasMouseCaptured;
    private GameView gameView;

    public PauseMenuController(GameController gameController, Main game, GameView gameView) {
        this.gameController = gameController;
        this.game = game;
        this.isPaused = false;
        this.gameView = gameView;
    }
    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    public void togglePause() {
        isPaused = !isPaused;

        if (isPaused) {
            wasMouseCaptured = Gdx.input.isCursorCatched();
            Gdx.input.setCursorCatched(false);
        }
        else {
            Gdx.input.setCursorCatched(wasMouseCaptured);
        }
    }

    public void toggleGrayscale() {
        if (gameView != null) {
            gameView.toggleGrayscale();
        }
    }
    public boolean isPaused() {
        return isPaused;
    }

    public void resume() {
        if (isPaused) {
            isPaused = false;
            // Restore previous mouse state
            Gdx.input.setCursorCatched(wasMouseCaptured);
        }
    }
    public Map<String, Integer> getPlayerAbilityNames() {
        Map<String, Integer> abilityCount = new HashMap<>();
        Player player = gameController.getPlayer();
        if (player.getBulletSpreadLevel() > 0) {
            abilityCount.put("Procrease", player.getBulletSpreadLevel());
        }
        if (player.isAmmoIncreaseActive()) {
            abilityCount.put("Amocrease", 1);
        }
        if (player.isVitalityActive()) {
            abilityCount.put("Vitality", 1);
        }
        if (player.getDamageMultiplier() > 1.0f) {
            abilityCount.put("Damager", 1);
        }
        if (player.getSpeedMultiplier() > 1.0f) {
            abilityCount.put("Speedy", 1);
        }
        return abilityCount;
    }
    public Main getGame() {
        return game;
    }
    public GameController getGameController() {
        return gameController;
    }
    public void exitGame() {
        game.setScreen(new PreMenuView(game));
    }
    public Player getPlayer() {
        return gameController.getPlayer();
    }
}
