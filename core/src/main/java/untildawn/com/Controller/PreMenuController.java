package untildawn.com.Controller;

import com.badlogic.gdx.Gdx;
import untildawn.com.Main;
import untildawn.com.View.*;

public class PreMenuController {
    private final Main game;

    public PreMenuController(Main game) {
        this.game = game;
    }
    public void exitGame() {
        Gdx.app.exit();
    }
    public void openCredits() {
        game.setScreen(new CreditsView(game));
    }
    public void openRegisterMenu() {
        game.setScreen(new RegisterMenuView(game));
    }
    public void goBack() {
        game.setScreen(new PreMenuView(game));
    }
}
