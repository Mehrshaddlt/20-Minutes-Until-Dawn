package untildawn.com.Model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import untildawn.com.Controller.GameController;
import java.io.*;

public class SaveGameManager {
    private static final String SAVE_FILE = "lastgame.sav";

    public static void saveLastGame(GameController gameController) {
        try {
            FileHandle file = Gdx.files.local(SAVE_FILE);
            FileOutputStream fos = new FileOutputStream(file.file());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            Player player = gameController.getPlayer();
            oos.writeInt(gameController.getTotalGameTimeInSeconds());
            oos.writeFloat(player.getPosition().x);
            oos.writeFloat(player.getPosition().y);
            oos.writeInt(player.getCurrentHealth());
            oos.writeUTF(player.getCurrentWeapon());
            oos.close();
        } catch (IOException e) {
            Gdx.app.error("SaveGame", "Error saving game", e);
        }
    }

    public static void loadLastGame(GameController gameController) {
        try {
            FileHandle file = Gdx.files.local(SAVE_FILE);
            FileInputStream fis = new FileInputStream(file.file());
            ObjectInputStream ois = new ObjectInputStream(fis);
            int timeLeft = ois.readInt();
            float x = ois.readFloat();
            float y = ois.readFloat();
            int health = ois.readInt();
            String weapon = ois.readUTF();
            Player player = gameController.getPlayer();
            player.setPosition(x, y);
            player.takeDamage(player.getCurrentHealth());
            player.heal(health);
            player.setCurrentWeapon(weapon);
            ois.close();
        } catch (IOException e) {
            Gdx.app.error("SaveGame", "Error loading game", e);
        }
    }
    public static boolean hasLastGame() {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        return file.exists();
    }
}
