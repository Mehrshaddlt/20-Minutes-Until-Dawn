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
        boolean allLoaded = true;
        for (int i = 0; i <= 5; i++) {
            try {
                Texture texture = new Texture(Gdx.files.internal("Level/T_LevelUpFX_" + i + ".png"));
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                textures.add(texture);
                frames.add(new TextureRegion(texture));
                System.out.println("[SUCCESS] Loaded frame " + i + " | Total frames: " + frames.size);
            }
            catch (Exception e) {
                System.err.println("[ERROR] Failed to load frame " + i + ": " + e.getMessage());
                allLoaded = false;
            }
        }
        if (!allLoaded) {
            System.err.println("[WARNING] Some frames failed to load. Animation may not work correctly.");
        }
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
        stateTime = 0f;
        position.set(playerPosition);
        System.out.println("[ANIMATION] Started at position " + position);
    }
    public void update(float delta, Vector2 playerPosition) {
        if (isActive) {
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
            float scaledWidth = width * 1.5f;
            float scaledHeight = height * 3f;
            batch.draw(
                currentFrame,
                position.x - scaledWidth/2,
                position.y - 30f,
                scaledWidth,
                scaledHeight
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
