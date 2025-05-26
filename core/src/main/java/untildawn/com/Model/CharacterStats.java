package untildawn.com.Model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.HashMap;
import java.util.Map;

public class CharacterStats {
    private static final Map<String, Stats> characterStats = new HashMap<>();

    // UI elements
    private static Texture ammoIcon;
    private static Texture[] heartFrames;
    private static Animation<TextureRegion> heartAnimation;
    private static float animationSpeed = 0.5f;

    static {
        // Initialize character stats
        characterStats.put("shana", new Stats(4, 4));
        characterStats.put("diamond", new Stats(7, 1));
        characterStats.put("scarlett", new Stats(3, 5));
        characterStats.put("lilith", new Stats(5, 3));
        characterStats.put("dasher", new Stats(2, 10));

        // Load UI elements
        loadUIAssets();
    }

    private static void loadUIAssets() {
        // Load ammo icon
        ammoIcon = new Texture(Gdx.files.internal("assets/HealthAndAmmo/Ammo/T_AmmoIcon.png"));

        // Load heart animation frames
        heartFrames = new Texture[4];
        TextureRegion[] heartRegions = new TextureRegion[3]; // Only frames 0-2 for animation

        for (int i = 0; i < 3; i++) {
            heartFrames[i] = new Texture(Gdx.files.internal("assets/HealthAndAmmo/Health/HeartAnimation_" + i + ".png"));
            heartRegions[i] = new TextureRegion(heartFrames[i]);
        }

        // Frame 3 is empty heart
        heartFrames[3] = new Texture(Gdx.files.internal("assets/HealthAndAmmo/Health/HeartAnimation_3.png"));

        // Create heart animation
        heartAnimation = new Animation<>(animationSpeed, heartRegions);
    }

    public static Stats getStats(String character) {
        return characterStats.getOrDefault(character.toLowerCase(), new Stats(4, 4)); // Default to Shana's stats
    }

    // UI asset getters
    public static Texture getAmmoIcon() {
        return ammoIcon;
    }

    public static Animation<TextureRegion> getHeartAnimation() {
        return heartAnimation;
    }

    public static Texture getEmptyHeart() {
        return heartFrames[3];
    }

    public static void dispose() {
        if (ammoIcon != null) ammoIcon.dispose();
        if (heartFrames != null) {
            for (Texture texture : heartFrames) {
                if (texture != null) texture.dispose();
            }
        }
    }

    public static class Stats {
        private final int maxHealth;
        private final int speedMultiplier;

        public Stats(int maxHealth, int speedMultiplier) {
            this.maxHealth = maxHealth;
            this.speedMultiplier = speedMultiplier;
        }

        public int getMaxHealth() {
            return maxHealth;
        }

        public int getSpeedMultiplier() {
            return speedMultiplier;
        }
    }
}
