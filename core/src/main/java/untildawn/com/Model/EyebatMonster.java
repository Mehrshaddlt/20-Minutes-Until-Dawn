package untildawn.com.Model;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class EyebatMonster extends Enemy {
    private static final float ATTACK_COOLDOWN = 3.0f;
    private static final float HEALTH = 50f;
    private static final float MOVEMENT_SPEED = 150f;
    private float attackTimer = 0f;
    private Animation<TextureRegion> flyingAnimation;
    private float animationTime = 0f;
    private float width = 48;
    private float height = 48;

    private WeaponsManager weaponsManager;

    public EyebatMonster(float x, float y, TextureRegion[] frames, WeaponsManager weaponsManager) {
        super(x, y, 50);
        this.health = HEALTH;
        this.weaponsManager = weaponsManager;
        flyingAnimation = new Animation<>(0.1f, frames);
        flyingAnimation.setPlayMode(Animation.PlayMode.LOOP);

        this.animationTime = (float)Math.random() * 2.0f;
        this.attackTimer = (float)Math.random() * ATTACK_COOLDOWN;
    }

    @Override
    public void update(float delta, Player player) {
        if (!isActive()) return;
        updateHitFlash(delta);
        position.add(velocity.x * delta, velocity.y * delta);
        velocity.scl(0.95f);
        if (velocity.len2() < 1000f) {
            animationTime += delta;
            Vector2 directionToPlayer = new Vector2(
                player.getPosition().x - position.x,
                player.getPosition().y - position.y
            );
            float distanceToPlayer = directionToPlayer.len();
            if (distanceToPlayer > 0) {
                directionToPlayer.nor();
            }
            float idealDistance = 250f;
            float distanceFactor = (distanceToPlayer - idealDistance) / 200f;
            distanceFactor = Math.max(-1f, Math.min(1f, distanceFactor));
            position.x += directionToPlayer.x * MOVEMENT_SPEED * distanceFactor * delta;
            position.y += directionToPlayer.y * MOVEMENT_SPEED * distanceFactor * delta;
            attackTimer += delta;
            if (attackTimer >= ATTACK_COOLDOWN) {
                attackTimer = 0;
                shootAtPlayer(player);
            }
        }
    }

    private void shootAtPlayer(Player player) {
        if (weaponsManager == null) return;
        SoundManager.getInstance().play("projectile", 0.5f);
        weaponsManager.createEnemyBullet(
            new Vector2(position.x, position.y),
            new Vector2(player.getPosition().x, player.getPosition().y)
        );
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isActive()) return;
        if (isHitFlashing) {
            batch.setColor(1, 0, 0, 1);
        }
        TextureRegion currentFrame = flyingAnimation.getKeyFrame(animationTime);
        batch.draw(currentFrame,
            position.x - width/2, position.y - height/2,
            width, height
        );
        if (isHitFlashing) {
            batch.setColor(1, 1, 1, 1);
        }
    }
}
