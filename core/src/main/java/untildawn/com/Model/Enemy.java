package untildawn.com.Model;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public abstract class Enemy {
    protected Vector2 position;
    protected float health;
    protected boolean active;
    protected boolean isHitFlashing = false;
    protected float hitFlashTime = 0;
    protected final float HIT_FLASH_DURATION = 0.1f;
    private boolean hasAwardedXP = false;
    protected Vector2 velocity = new Vector2();
    protected float knockbackResistance = 1.0f;

    public Enemy(float x, float y, float health) {
        this.position = new Vector2(x, y);
        this.health = health;
        this.active = true;
    }

    public void startHitFlash() {
        isHitFlashing = true;
        hitFlashTime = 0;
    }

    public void updateHitFlash(float delta) {
        if (isHitFlashing) {
            hitFlashTime += delta;
            if (hitFlashTime > HIT_FLASH_DURATION) {
                isHitFlashing = false;
            }
        }
    }

    public abstract void update(float delta, Player player);
    public abstract void render(SpriteBatch batch);

    public boolean isActive() {
        return active;
    }

    public Vector2 getPosition() {
        return position;
    }

    public boolean takeDamage(float damage) {
        health -= damage;
        startHitFlash();

        if (health <= 0) {
            active = false;
            return true;
        }
        return false;
    }
    public boolean hasAwardedXP() {
        return hasAwardedXP;
    }
    public void setHasAwardedXP(boolean hasAwardedXP) {
        this.hasAwardedXP = hasAwardedXP;
    }
    public float distanceToPlayer(Player player) {
        return position.dst(player.getPosition());
    }
    public float getCollisionRadius() {
        return 20f;
    }
}
