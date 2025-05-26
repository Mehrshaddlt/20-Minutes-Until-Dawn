package untildawn.com.Model;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TreeMonster extends Enemy {
    private static final float FAR_DISTANCE = 180f;
    private static final float CLOSE_DISTANCE = 60f;
    private float collisionRadius;

    private TextureRegion[] textures;
    private int currentFrame;

    private int damageAmount; // Changed from float to int
    private float damageInterval;
    private float damageTimer;

    public TreeMonster(float x, float y, TextureRegion[] textures) {
        super(x, y, 45000f); // 100 health
        this.textures = textures;
        this.currentFrame = 0;
        this.damageAmount = 1; // 1 heart of damage
        this.damageInterval = 1f; // Damage once per second
        this.damageTimer = 0f;

        this.collisionRadius = textures[0].getRegionWidth() * 0.4f;
    }
    public boolean collidesWithPoint(float x, float y) {
        float dx = position.x - x;
        float dy = position.y - y;
        return dx * dx + dy * dy < collisionRadius * collisionRadius;
    }

    @Override
    public void update(float delta, Player player) {
        if (!active) return;

        updateHitFlash(delta);

        float distance = distanceToPlayer(player);

        // Update animation frame based on distance
        if (distance > FAR_DISTANCE) {
            currentFrame = 0; // Normal tree when far
        } else if (distance > CLOSE_DISTANCE) {
            currentFrame = 1; // Eye slightly open when closer
        } else {
            currentFrame = 2; // Eye fully open when very close

            // Deal damage when close
            damageTimer += delta;
            if (damageTimer >= damageInterval) {
                player.takeDamage(damageAmount); // Now passing an int (1 heart)
                damageTimer = 0f;
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!active) return;
        if (isHitFlashing) {
            batch.setColor(1, 0, 0, 1);  // Red color
        }
        TextureRegion currentTexture = textures[currentFrame];
        batch.draw(currentTexture,
            position.x - currentTexture.getRegionWidth() / 2,
            position.y - currentTexture.getRegionHeight() / 2);
        if (isHitFlashing) {
            batch.setColor(1, 1, 1, 1);  // Reset to white
        }
    }
}
