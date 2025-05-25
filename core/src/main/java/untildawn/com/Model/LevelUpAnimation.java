package untildawn.com.Model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class LevelUpAnimation implements Disposable {
    private Animation<TextureRegion> animation;
    private float stateTime;
    private boolean isActive;
    private Vector2 position;
    private float width = 64f;
    private float height = 128f;
    private Array<Texture> textures;

    public LevelUpAnimation() {
        textures = new Array<>();
        Array<TextureRegion> frames = new Array<>();

        // Load frames and report status
        boolean allLoaded = true;
        for (int i = 0; i <= 5; i++) {
            try {
                // Note: paths are case-sensitive!
                Texture texture = new Texture(Gdx.files.internal("Level/T_LevelUpFX_" + i + ".png"));
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                textures.add(texture);
                frames.add(new TextureRegion(texture));
                System.out.println("[SUCCESS] Loaded frame " + i + " | Total frames: " + frames.size);
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to load frame " + i + ": " + e.getMessage());
                allLoaded = false;
            }
        }

        if (!allLoaded) {
            System.err.println("[WARNING] Some frames failed to load. Animation may not work correctly.");
        }

        // Create animation with 0.2 second per frame - will cycle through in 1 second
        animation = new Animation<>(0.1f, frames);
        stateTime = 0f;
        isActive = false;
        position = new Vector2();
    }
    public float getStateTime() {
        return stateTime;
    }
    public void start(Vector2 playerPosition) {
        isActive = true;
        stateTime = 0f; // Reset to start from first frame
        position.set(playerPosition);
        System.out.println("[ANIMATION] Started at position " + position);
    }

    public void update(float delta, Vector2 playerPosition) {
        if (isActive) {
            // This is the key line to advance the animation
            stateTime += delta;
            position.set(playerPosition);

            int frame = animation.getKeyFrameIndex(stateTime);
            System.out.println("[ANIMATION] Frame: " + frame + " Time: " + stateTime);

            if (animation.isAnimationFinished(stateTime)) {
                isActive = false;
                System.out.println("[ANIMATION] Completed at time " + stateTime);
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (isActive) {
            TextureRegion currentFrame = animation.getKeyFrame(stateTime, false);

            // Make the animation much larger and position it better
            float scaledWidth = width * 1.5f;   // Triple the width
            float scaledHeight = height * 3f; // Quadruple the height

            batch.draw(
                currentFrame,
                position.x - scaledWidth/2,   // Center horizontally
                position.y - 30f,             // Move down by 40 pixels from player position
                scaledWidth,                  // Use the larger width
                scaledHeight                  // Use the larger height
            );
        }
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public void dispose() {
        for (Texture texture : textures) {
            if (texture != null) texture.dispose();
        }
    }
}
