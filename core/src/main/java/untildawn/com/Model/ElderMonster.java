package untildawn.com.Model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class ElderMonster extends Enemy {
    private static final float MAX_HP = 400f;
    private static final float MOVEMENT_SPEED = 150f;
    private static final float DASH_COOLDOWN = 5f;
    private static final float DASH_SPEED = 500f;
    private static final float DASH_DURATION = 0.5f;
    private static final float ARENA_SHRINK_INTERVAL = 5f;
    private static final float ARENA_SHRINK_AMOUNT = 50f;
    private static final float PLAYER_DAMAGE_AMOUNT = 1f;
    private static final float PLAYER_DAMAGE_COOLDOWN = 1f;
    private float playerDamageCooldown = 0f;
    private TextureRegion[] wallFrames;
    private float wallAnimationTime;
    private int currentWallFrame;
    private Vector2 arenaCenter;
    private float arenaWidth;
    private float arenaHeight;
    private float shrinkTimer;
    private Texture monsterTexture;
    private boolean isChargingDash;
    private boolean isDashing;
    private float dashCooldownTimer;
    private float currentDashTime;

    public ElderMonster(float x, float y, Player player) {
        super(x, y, MAX_HP);
        isChargingDash = false;
        isDashing = false;
        dashCooldownTimer = DASH_COOLDOWN;
        currentDashTime = 0;
        shrinkTimer = 0;
        playerDamageCooldown = PLAYER_DAMAGE_COOLDOWN;
        arenaWidth = Gdx.graphics.getWidth();
        arenaHeight = Gdx.graphics.getHeight();
        arenaCenter = new Vector2(player.getPosition().x, player.getPosition().y);
        monsterTexture = new Texture(Gdx.files.internal("ElderMonster/ElderMonster.png"));
        loadWallTextures();
    }
    private void loadWallTextures() {
        wallFrames = new TextureRegion[6];
        for (int i = 0; i < 6; i++) {
            Texture frame = new Texture(Gdx.files.internal("ElderMonster/T_ElectricWall" + (i + 1) + ".png"));
            wallFrames[i] = new TextureRegion(frame);
        }
        wallAnimationTime = 0;
        currentWallFrame = 0;
    }

    @Override
    public void update(float deltaTime, Player player) {
        if (!isActive()) return;
        if (playerDamageCooldown > 0) {
            playerDamageCooldown -= deltaTime;
        }
        wallAnimationTime += deltaTime;
        if (wallAnimationTime >= 0.1f) {
            wallAnimationTime = 0;
            currentWallFrame = (currentWallFrame + 1) % 6;
        }
        shrinkTimer += deltaTime;
        if (shrinkTimer >= ARENA_SHRINK_INTERVAL) {
            shrinkTimer = 0;
            arenaWidth = Math.max(400, arenaWidth - ARENA_SHRINK_AMOUNT);
            arenaHeight = Math.max(300, arenaHeight - ARENA_SHRINK_AMOUNT);
        }
        checkWallCollision(player);
        if (isDashing) {
            currentDashTime += deltaTime;
            if (currentDashTime >= DASH_DURATION) {
                isDashing = false;
                currentDashTime = 0;
                velocity.setZero();
            }
        }
        else {
            dashCooldownTimer -= deltaTime;
            if (dashCooldownTimer <= 0) {
                Vector2 dashDir = player.getPosition().cpy().sub(position).nor();
                velocity.set(dashDir.scl(DASH_SPEED));
                isDashing = true;
                dashCooldownTimer = DASH_COOLDOWN;
            }
            else {
                Vector2 direction = player.getPosition().cpy().sub(position).nor();
                velocity.set(direction.scl(MOVEMENT_SPEED));
            }
        }
        position.add(velocity.x * deltaTime, velocity.y * deltaTime);
        float distanceToPlayer = player.getPosition().dst(position);
        if (distanceToPlayer < 64 && playerDamageCooldown <= 0) {
            player.takeDamage(1);
            playerDamageCooldown = PLAYER_DAMAGE_COOLDOWN;
        }
        float halfWidth = arenaWidth / 2;
        float halfHeight = arenaHeight / 2;
        position.x = Math.max(arenaCenter.x - halfWidth + 32, Math.min(position.x, arenaCenter.x + halfWidth - 32));
        position.y = Math.max(arenaCenter.y - halfHeight + 32, Math.min(position.y, arenaCenter.y + halfHeight - 32));
        updateHitFlash(deltaTime);
    }

    private void checkWallCollision(Player player) {
        if (playerDamageCooldown > 0) return;
        float halfWidth = arenaWidth / 2;
        float halfHeight = arenaHeight / 2;
        Vector2 playerPos = player.getPosition();
        float wallThickness = 24;
        if (playerPos.x < arenaCenter.x - halfWidth + wallThickness) {
            player.takeDamage(1);
            player.setPosition(arenaCenter.x - halfWidth + wallThickness, playerPos.y);
            playerDamageCooldown = PLAYER_DAMAGE_COOLDOWN;
        }
        else if (playerPos.x > arenaCenter.x + halfWidth - wallThickness) {
            player.takeDamage(1);
            player.setPosition(arenaCenter.x + halfWidth - wallThickness, playerPos.y);
            playerDamageCooldown = PLAYER_DAMAGE_COOLDOWN;
        }
        if (playerPos.y < arenaCenter.y - halfHeight + wallThickness) {
            player.takeDamage(1);
            player.setPosition(playerPos.x, arenaCenter.y - halfHeight + wallThickness);
            playerDamageCooldown = PLAYER_DAMAGE_COOLDOWN;
        }
        else if (playerPos.y > arenaCenter.y + halfHeight - wallThickness) {
            player.takeDamage(1);
            player.setPosition(playerPos.x, arenaCenter.y + halfHeight - wallThickness);
            playerDamageCooldown = PLAYER_DAMAGE_COOLDOWN;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isActive()) return;
        float halfWidth = arenaWidth / 2;
        float halfHeight = arenaHeight / 2;
        TextureRegion currentWall = wallFrames[currentWallFrame];
        float wallSize = 24;
        float leftX = arenaCenter.x - halfWidth;
        float rightX = arenaCenter.x + halfWidth;
        float bottomY = arenaCenter.y - halfHeight;
        float topY = arenaCenter.y + halfHeight;
        for (float x = leftX; x < rightX; x += wallSize) {
            batch.draw(currentWall, x, topY - wallSize, wallSize, wallSize);
            batch.draw(currentWall, x, bottomY, wallSize, wallSize);
        }
        for (float y = bottomY; y < topY; y += wallSize) {
            batch.draw(currentWall,
                leftX, y,
                0, 0,
                wallSize, wallSize,
                1, 1,
                270);
            batch.draw(currentWall,
                rightX - wallSize, y,
                0, 0,
                wallSize, wallSize,
                1, 1,
                90);
        }
        if (isDashing) {
            batch.setColor(1, 0.84f, 0, 1);
        } else if (isHitFlashing) {
            batch.setColor(1, 0, 0, 1);
        }

        batch.draw(monsterTexture, position.x - 32, position.y - 32, 64, 64);
        batch.setColor(1, 1, 1, 1);
    }
}
