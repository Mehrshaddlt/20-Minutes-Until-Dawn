package untildawn.com.Model;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class EffectAnimation {
    private Animation<TextureRegion> animation;
    private float stateTime = 0;
    private Vector2 position;
    private boolean isFinished = false;
    private float scale;

    public EffectAnimation(TextureRegion[] frames, float frameDuration, Vector2 position, float scale) {
        this.animation = new Animation<>(frameDuration, frames);
        this.position = new Vector2(position);
        this.scale = scale;
    }

    public void update(float delta) {
        stateTime += delta;
        if (animation.isAnimationFinished(stateTime)) {
            isFinished = true;
        }
    }

    public void render(SpriteBatch batch) {
        if (!isFinished) {
            TextureRegion currentFrame = animation.getKeyFrame(stateTime);
            float width = currentFrame.getRegionWidth() * scale;
            float height = currentFrame.getRegionHeight() * scale;
            batch.draw(currentFrame, position.x - width/2, position.y - height/2, width, height);
        }
    }

    public boolean isFinished() {
        return isFinished;
    }
}
