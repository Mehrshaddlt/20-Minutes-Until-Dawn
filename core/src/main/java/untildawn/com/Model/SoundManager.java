package untildawn.com.Model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static SoundManager instance;
    private Map<String, Sound> sounds;
    private Map<String, Long> loopingSoundIds;
    private boolean soundEnabled = true;
    private float volume = 1.0f;

    private SoundManager() {
        sounds = new HashMap<>();
        loopingSoundIds = new HashMap<>();
        loadSounds();
    }
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }
    private void loadSounds() {
        try {
            sounds.put("bateyeDeath", Gdx.audio.newSound(Gdx.files.internal("SFX/Bateye_Death.wav")));
            sounds.put("brainmonsterDeath", Gdx.audio.newSound(Gdx.files.internal("SFX/Brainmonster_Death.wav")));
            sounds.put("footsteps", Gdx.audio.newSound(Gdx.files.internal("SFX/Footsteps.wav")));
            sounds.put("impact", Gdx.audio.newSound(Gdx.files.internal("SFX/Impact.wav")));
            sounds.put("levelup", Gdx.audio.newSound(Gdx.files.internal("SFX/Levelup.wav")));
            sounds.put("projectile", Gdx.audio.newSound(Gdx.files.internal("SFX/Projectile.wav")));
            sounds.put("lost", Gdx.audio.newSound(Gdx.files.internal("SFX/Lost.wav")));
            sounds.put("lowhealth", Gdx.audio.newSound(Gdx.files.internal("SFX/Lowhealth.wav")));
            sounds.put("reload", Gdx.audio.newSound(Gdx.files.internal("SFX/Reload.wav")));
            sounds.put("select", Gdx.audio.newSound(Gdx.files.internal("SFX/Select.wav")));
            sounds.put("shot", Gdx.audio.newSound(Gdx.files.internal("SFX/Shot.wav")));
            sounds.put("win", Gdx.audio.newSound(Gdx.files.internal("SFX/Win.wav")));
            sounds.put("xp_pickup", Gdx.audio.newSound(Gdx.files.internal("SFX/XP_Pickup.wav")));
            Gdx.app.debug("SoundManager", "Loaded sound files successfully");
        }
        catch (Exception e) {
            Gdx.app.error("SoundManager", "Error loading sounds: " + e.getMessage());
        }
    }

    public void play(String soundName) {
        if (soundEnabled && sounds.containsKey(soundName)) {
            sounds.get(soundName).play(volume);
        }
    }
    public void play(String soundName, float volumeModifier) {
        if (soundEnabled && sounds.containsKey(soundName)) {
            sounds.get(soundName).play(volume * volumeModifier);
        }
    }
    public void startLoopingSound(String soundName, float volumeModifier) {
        if (!soundEnabled || !sounds.containsKey(soundName)) {
            return;
        }
        if (!loopingSoundIds.containsKey(soundName)) {
            Sound sound = sounds.get(soundName);
            if (sound != null) {
                long id = sound.loop(volume * volumeModifier);
                loopingSoundIds.put(soundName, id);
                Gdx.app.debug("SoundManager", "Started looping " + soundName + " with id: " + id);
            }
        }
    }
    public void stopLoopingSound(String soundName) {
        if (sounds.containsKey(soundName) && loopingSoundIds.containsKey(soundName)) {
            Long soundId = loopingSoundIds.get(soundName);
            if (soundId != null) {
                sounds.get(soundName).stop(soundId);
                Gdx.app.debug("SoundManager", "Stopped looping " + soundName);
            }
            loopingSoundIds.remove(soundName);
        }
    }
    public boolean isLoopingSoundPlaying(String soundName) {
        return loopingSoundIds.containsKey(soundName);
    }
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) {
            for (String soundName : loopingSoundIds.keySet()) {
                stopLoopingSound(soundName);
            }
        }
    }
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    public void setVolume(float volume) {
        this.volume = Math.max(0, Math.min(1, volume));
    }
    public float getVolume() {
        return volume;
    }
    public void dispose() {
        for (Sound sound : sounds.values()) {
            sound.dispose();
        }
        sounds.clear();
        loopingSoundIds.clear();
    }
}
