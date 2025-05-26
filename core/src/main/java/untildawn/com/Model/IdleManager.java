package untildawn.com.Model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.HashMap;
import java.util.Map;

public class IdleManager {
    // Character names for consistent reference
    public static final String[] HERO_NAMES = {
        "Dasher", "Diamond", "Lilith", "Scarlett", "Shana"
    };

    // Animation properties
    private static final int FRAME_COUNT = 5;
    private static final String PATH_FORMAT = "Heroes/Idle/Idle_%d_%s.png";

    // Store animations for each character
    private final Map<String, Array<TextureRegion>> heroIdleAnimations = new HashMap<>();

    public IdleManager() {
        loadIdleAnimations();
    }

    private void loadIdleAnimations() {
        for (String hero : HERO_NAMES) {
            Array<TextureRegion> frames = new Array<>();
            for (int i = 1; i <= FRAME_COUNT; i++) {
                String path = String.format(PATH_FORMAT, i, hero);
                try {
                    Texture texture = new Texture(Gdx.files.internal(path));
                    frames.add(new TextureRegion(texture));
                } catch (Exception e) {
                    Gdx.app.error("IdleManager", "Failed to load: " + path, e);
                }
            }
            heroIdleAnimations.put(hero, frames);
        }
    }

    public Array<TextureRegion> getIdleFrames(String heroName) {
        Array<TextureRegion> frames = heroIdleAnimations.get(heroName);
        // Fallback to Diamond if the requested hero doesn't exist
        if (frames == null && heroIdleAnimations.containsKey("Diamond")) {
            frames = heroIdleAnimations.get("Diamond");
        }
        return frames;
    }

    public void dispose() {
        // Properly dispose all textures
        for (Array<TextureRegion> frames : heroIdleAnimations.values()) {
            for (TextureRegion frame : frames) {
                if (frame.getTexture() != null) {
                    frame.getTexture().dispose();
                }
            }
        }
        heroIdleAnimations.clear();
    }
}
