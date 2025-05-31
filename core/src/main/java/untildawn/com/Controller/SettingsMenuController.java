package untildawn.com.Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import untildawn.com.Main;
import untildawn.com.Model.User;
import untildawn.com.Model.SoundManager;
import untildawn.com.Model.MusicManager;
import untildawn.com.View.MainMenuView;

public class SettingsMenuController {
    private final Main game;
    private final User currentUser;
    private int upKey = Input.Keys.W;
    private int leftKey = Input.Keys.A;
    private int downKey = Input.Keys.S;
    private int rightKey = Input.Keys.D;
    private int autoaimKey = Input.Keys.SPACE;
    private float musicVolume = 0.7f;
    private boolean sfxEnabled = true;
    private String selectedMusic = "Menu 1";
    private boolean autoReloadEnabled = true;
    private Preferences prefs;
    public SettingsMenuController(Main game, User currentUser) {
        this.game = game;
        this.currentUser = currentUser;
        prefs = Gdx.app.getPreferences("untildawn_settings");
        loadSettings();
    }

    public void loadSettings() {
        upKey = prefs.getInteger("key_UP", Input.Keys.W);
        leftKey = prefs.getInteger("key_LEFT", Input.Keys.A);
        downKey = prefs.getInteger("key_DOWN", Input.Keys.S);
        rightKey = prefs.getInteger("key_RIGHT", Input.Keys.D);
        autoaimKey = prefs.getInteger("key_AUTOAIM", Input.Keys.SPACE);
        musicVolume = prefs.getFloat("music_volume", 0.7f);
        sfxEnabled = prefs.getBoolean("sfx_enabled", true);
        selectedMusic = prefs.getString("selected_music", "Menu 1");
        autoReloadEnabled = prefs.getBoolean("auto_reload_enabled", true);
        SoundManager.getInstance().setVolume(musicVolume);
        SoundManager.getInstance().setSoundEnabled(sfxEnabled);
        MusicManager.getInstance().setVolume(musicVolume);
        if (selectedMusic.equals("Menu 1") || selectedMusic.equals("Menu 2")) {
            MusicManager.getInstance().playMusic(selectedMusic);
        }
    }

    public void saveSettings() {
        prefs.putInteger("key_UP", upKey);
        prefs.putInteger("key_LEFT", leftKey);
        prefs.putInteger("key_DOWN", downKey);
        prefs.putInteger("key_RIGHT", rightKey);
        prefs.putInteger("key_AUTOAIM", autoaimKey);
        prefs.putFloat("music_volume", musicVolume);
        prefs.putBoolean("sfx_enabled", sfxEnabled);
        prefs.putString("selected_music", selectedMusic);
        prefs.putBoolean("auto_reload_enabled", autoReloadEnabled);
        prefs.flush();
    }

    public int getKeyBinding(String action) {
        switch (action) {
            case "UP": return upKey;
            case "LEFT": return leftKey;
            case "DOWN": return downKey;
            case "RIGHT": return rightKey;
            case "AUTOAIM": return autoaimKey;
            default: return Input.Keys.UNKNOWN;
        }
    }

    public void setKeyBinding(String action, int keycode) {
        switch (action) {
            case "UP": upKey = keycode; break;
            case "LEFT": leftKey = keycode; break;
            case "DOWN": downKey = keycode; break;
            case "RIGHT": rightKey = keycode; break;
            case "AUTOAIM": autoaimKey = keycode; break;
        }
        prefs.putInteger("key_" + action, keycode);
        prefs.flush();
    }
    public float getMusicVolume() {
        return musicVolume;
    }
    public void setMusicVolume(float volume) {
        this.musicVolume = volume;
        SoundManager.getInstance().setVolume(volume);
        MusicManager.getInstance().setVolume(volume);
    }
    public boolean isSfxEnabled() {
        return sfxEnabled;
    }
    public void setSfxEnabled(boolean enabled) {
        this.sfxEnabled = enabled;
        SoundManager.getInstance().setSoundEnabled(enabled);
    }
    public String getSelectedMusic() {
        return selectedMusic;
    }
    public void setSelectedMusic(String music) {
        this.selectedMusic = music;
        MusicManager.getInstance().playMusic(music);
    }
    public boolean isAutoReloadEnabled() {
        return autoReloadEnabled;
    }
    public void setAutoReloadEnabled(boolean enabled) {
        this.autoReloadEnabled = enabled;
        prefs.putBoolean("auto_reload_enabled", enabled);
        prefs.flush();
    }
    public void backToMainMenu() {
        game.setScreen(new MainMenuView(game, currentUser));
    }
}
