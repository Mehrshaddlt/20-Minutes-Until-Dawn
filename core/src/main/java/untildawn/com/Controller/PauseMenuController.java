package untildawn.com.Controller;

import com.badlogic.gdx.Gdx;
import untildawn.com.Main;
import untildawn.com.Model.Player;
import untildawn.com.View.PreMenuView;

import java.util.HashMap;
import java.util.Map;

public class PauseMenuController {
    private boolean isPaused;
    private final GameController gameController;
    private final Main game;
    private boolean wasMouseCaptured;

    public PauseMenuController(GameController gameController, Main game) {
        this.gameController = gameController;
        this.game = game;
        this.isPaused = false;
    }
    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    public void togglePause() {
        isPaused = !isPaused;

        if (isPaused) {
            // Store mouse state and show cursor when pausing
            wasMouseCaptured = Gdx.input.isCursorCatched();
            Gdx.input.setCursorCatched(false);
        } else {
            // Restore previous mouse state when unpausing
            Gdx.input.setCursorCatched(wasMouseCaptured);
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

        // Procrease (extra bullets)
        if (player.getBulletSpreadLevel() > 0) {
            abilityCount.put("Procrease", player.getBulletSpreadLevel());
        }

        // Ammo increase
        if (player.isAmmoIncreaseActive()) {
            abilityCount.put("Amocrease", 1);
        }

        if (player.isVitalityActive()) {
            abilityCount.put("Vitality", 1);
        }

        // Damage multiplier (if active)
        if (player.getDamageMultiplier() > 1.0f) {
            abilityCount.put("Damager", 1);
        }

        // Speed multiplier (if active)
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
