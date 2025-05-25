package untildawn.com.Controller;

import com.badlogic.gdx.Gdx;
import untildawn.com.Main;
import untildawn.com.Model.User;
import untildawn.com.View.GameView;
import untildawn.com.View.MainMenuView;

public class PreGameMenuController {
    private final Main game;
    private final User currentUser;
    private String selectedTime = "20 Minutes";

    public PreGameMenuController(Main game, User currentUser) {
        this.game = game;
        this.currentUser = currentUser;
    }
    public void saveSelectedWeapon(String weaponName) {
        currentUser.setSelectedWeapon(weaponName);

    }
    public void backToMainMenu() {
        game.setScreen(new MainMenuView(game, currentUser));
    }
    public void saveSelectedTime(String time) {
        this.selectedTime = time;
    }
    public void saveSelectedHero(String heroName) {
        currentUser.setSelectedHero(heroName);
    }

    public void startGame() {
        Gdx.input.setInputProcessor(null);
        int minutes = Integer.parseInt(selectedTime.split(" ")[0]);
        game.setScreen(new GameView(game, currentUser, minutes));
    }
}
