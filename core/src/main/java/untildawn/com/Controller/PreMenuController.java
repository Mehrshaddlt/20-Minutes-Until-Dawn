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
        // Exit the application
        Gdx.app.exit();
    }
    public void openCredits() {
        // Navigate to the credits screen
        game.setScreen(new CreditsView(game));
    }
    public void openRegisterMenu() {
        game.setScreen(new RegisterMenuView(game));
    }
    public void goBack() {
        game.setScreen(new PreMenuView(game));
    }
    public void openLoginMenu() {
        // We'll implement this class next
        // game.setScreen(new LoginMenuView(game));
        System.out.println("Login menu clicked - to be implemented");
    }

    public void openMainMenu() {
        // We'll implement this class next
        // game.setScreen(new MainMenuView(game));
        System.out.println("Main menu clicked - to be implemented");
    }
}
