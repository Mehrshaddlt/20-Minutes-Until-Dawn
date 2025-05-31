package untildawn.com.Model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import java.util.HashMap;
import java.util.Map;

public class MusicManager {
    private static MusicManager instance;
    private Map<String, Music> musicTracks;
    private String currentTrack;
    private float volume = 0.7f;
    private boolean enabled = true;
    private String currentMusic;

    private MusicManager() {
        musicTracks = new HashMap<>();
        loadMusic();
    }
    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }
    private void loadMusic() {
        try {
            musicTracks.put("Menu 1", Gdx.audio.newMusic(Gdx.files.internal("Music/1.mp3")));
            musicTracks.put("Menu 2", Gdx.audio.newMusic(Gdx.files.internal("Music/2.mp3")));
            musicTracks.put("Gameplay", Gdx.audio.newMusic(Gdx.files.internal("Music/Gameplay.mp3")));
            for (Music music : musicTracks.values()) {
                music.setLooping(true);
            }
            Gdx.app.debug("MusicManager", "Loaded music files successfully");
        }
        catch (Exception e) {
            Gdx.app.error("MusicManager", "Error loading music: " + e.getMessage());
        }
    }

    public void playMusic(String trackName) {
        if (!enabled || !musicTracks.containsKey(trackName)) {
            return;
        }
        if (currentTrack != null && musicTracks.containsKey(currentTrack)) {
            musicTracks.get(currentTrack).stop();
        }
        Music music = musicTracks.get(trackName);
        music.setVolume(volume);
        music.play();
        currentTrack = trackName;
        this.currentMusic = trackName;
    }

    public void stopMusic() {
        if (currentTrack != null && musicTracks.containsKey(currentTrack)) {
            musicTracks.get(currentTrack).stop();
            currentTrack = null;
        }
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0, Math.min(1, volume));
        if (currentTrack != null && musicTracks.containsKey(currentTrack)) {
            musicTracks.get(currentTrack).setVolume(volume);
        }
    }

    public float getVolume() {
        return volume;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            stopMusic();
        } else if (currentTrack != null) {
            playMusic(currentTrack);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getCurrentTrack() {
        return currentTrack;
    }

    public void dispose() {
        for (Music music : musicTracks.values()) {
            music.dispose();
        }
        musicTracks.clear();
    }

    public String getCurrentMusic() {
        return currentMusic;
    }
}
