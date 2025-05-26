package untildawn.com.Model;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class BrainMonster extends Enemy {
    private static final float MOVE_SPEED = 60f;
    private static final float DAMAGE_RANGE = 50f;
    private static final float DAMAGE_COOLDOWN = 1f;
    private static final int MAX_HP = 25;

    private TextureRegion[] walkFrames;
    private float animationTime = 0;
    private float damageTimer = 0;

    public BrainMonster(float x, float y, TextureRegion[] walkFrames) {
        // Pass health as third parameter, not texture
        super(x, y, MAX_HP);
        this.walkFrames = walkFrames;
    }

    @Override
    public void update(float delta, Player player) {
        if (!isActive()) return;

        updateHitFlash(delta);

        animationTime += delta;

        // Move toward player
        Vector2 direction = new Vector2(player.getPosition()).sub(position);
        float distance = direction.len();

        if (distance > 0) {
            direction.nor();
            position.add(direction.x * MOVE_SPEED * delta, direction.y * MOVE_SPEED * delta);
        }

        // Damage player when close enough
        if (distance < DAMAGE_RANGE) {
            damageTimer += delta;
            if (damageTimer >= DAMAGE_COOLDOWN) {
                player.takeDamage(1);
                damageTimer = 0;
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isActive()) return;
        if (isHitFlashing) {
            batch.setColor(1, 0, 0, 1);  // Red color
        }
        // Animation logic: 7 frames (0-6), looping at 10 FPS
        int frameIndex = (int)(animationTime * 10) % 7;
        batch.draw(walkFrames[frameIndex],
            position.x - walkFrames[frameIndex].getRegionWidth()/2,
            position.y - walkFrames[frameIndex].getRegionHeight()/2);
        if (isHitFlashing) {
            batch.setColor(1, 1, 1, 1);  // Reset to white
        }
    }
}
