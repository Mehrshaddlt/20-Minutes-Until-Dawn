package untildawn.com.Model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Bullet {
    private Vector2 position;
    private Vector2 direction;
    private float speed;
    private float lifetime;
    private float maxLifetime;
    private boolean isEnemy; // New field to track enemy bullets
    private Array<Vector2> trailPositions;
    private int trailLength;
    private String weaponType;
    private Array<Enemy> hitEnemies = new Array<>();

    public Bullet(Vector2 position, Vector2 direction, float speed, float maxLifetime, int trailLength) {
        this.position = new Vector2(position);
        this.direction = new Vector2(direction).nor();
        this.speed = speed;
        this.maxLifetime = maxLifetime;
        this.lifetime = maxLifetime;
        this.isEnemy = false; // Default to player bullet
        this.trailLength = trailLength;

        // Initialize trail positions
        trailPositions = new Array<>(trailLength);
        for (int i = 0; i < trailLength; i++) {
            trailPositions.add(new Vector2(position));
        }
    }

    public void update(float delta) {
        // Update lifetime
        lifetime -= delta;
        if (lifetime <= 0) return;

        // Update position
        position.x += direction.x * speed * delta;
        position.y += direction.y * speed * delta;

        // Update trail positions (shift them forward)
        for (int i = 0; i < trailPositions.size - 1; i++) {
            trailPositions.get(i).set(trailPositions.get(i + 1));
        }
        trailPositions.get(trailPositions.size - 1).set(position);
    }

    public boolean isAlive() {
        return lifetime > 0;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Array<Vector2> getTrailPositions() {
        return trailPositions;
    }

    // Add these methods for enemy bullet support
    public boolean isEnemy() {
        return isEnemy;
    }
    public void addHitEnemy(Enemy enemy) {
        hitEnemies.add(enemy);
    }

    public boolean hasHitEnemy(Enemy enemy) {
        return hitEnemies.contains(enemy, true);
    }
    public void setIsEnemy(boolean isEnemy) {
        this.isEnemy = isEnemy;
    }
    public void setWeaponType(String weaponType) {
        this.weaponType = weaponType;
    }

    public String getWeaponType() {
        return weaponType;
    }
    public void kill() {
        this.lifetime = 0;
    }
}
