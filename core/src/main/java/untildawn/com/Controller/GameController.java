package untildawn.com.Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.Vector2;
import untildawn.com.Model.*;

public class GameController {
    private final Player player;
    private final MovementManager movementManager;
    private final IdleManager idleManager;
    private boolean canShoot = true;
    private float shootCooldown = 0;
    private final WeaponsManager weaponsManager;
    private EnemiesManager enemiesManager;
    private PauseMenuController pauseMenuController;
    private final int initialGameTimeInSeconds;
    private final SettingsMenuController settingsMenuController;
    public String currentWeapon;
    private boolean continuousFire = false;
    private final float smgFireRate = 0.08f;
    private float smgFireTimer = 0f;
    private Vector2 targetPosition = new Vector2();
    private float weaponAngle;
    private Vector2 autoAimTarget = null;
    private boolean autoAimEnabled = false;
    public GameController(User user, WeaponsManager weaponManager, EnemiesManager enemiesManager, int gameTimeInSeconds,  SettingsMenuController settingsMenuController) {
        String selectedHero = user.getSelectedHero() != null ? user.getSelectedHero() : "Diamond";
        player = new Player(selectedHero, 0, 0, user);
        movementManager = new MovementManager();
        idleManager = new IdleManager();
        this.weaponsManager = weaponManager;
        this.enemiesManager = enemiesManager;
        this.initialGameTimeInSeconds = gameTimeInSeconds;
        this.settingsMenuController = settingsMenuController;
    }
    public int getTotalGameTimeInSeconds() {
        return initialGameTimeInSeconds;
    }
    public void update(float delta) {
        if (currentWeapon == null) {
            currentWeapon = "Revolver";
        }

        Preferences prefs = Gdx.app.getPreferences("untildawn_settings");
        int upKey = prefs.getInteger("key_UP", Input.Keys.W);
        int downKey = prefs.getInteger("key_DOWN", Input.Keys.S);
        int leftKey = prefs.getInteger("key_LEFT", Input.Keys.A);
        int rightKey = prefs.getInteger("key_RIGHT", Input.Keys.D);
        int autoAimKey = prefs.getInteger("key_AUTOAIM", Input.Keys.SPACE);
        boolean moveUp = Gdx.input.isKeyPressed(upKey);
        boolean moveDown = Gdx.input.isKeyPressed(downKey);
        boolean moveLeft = Gdx.input.isKeyPressed(leftKey);
        boolean moveRight = Gdx.input.isKeyPressed(rightKey);
        boolean isRunning = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            weaponsManager.startReload(currentWeapon);
        }
        if (Gdx.input.isKeyPressed(autoAimKey)) {
            getAutoAimTarget();
        } else {
            autoAimTarget = null;
        }
        float moveAmount = isRunning ? player.runSpeed * delta : player.walkSpeed * delta;
        Vector2 position = player.getPosition();
        Vector2 newPosition = new Vector2(position);
        if (moveUp) {
            newPosition.set(position.x, position.y + moveAmount);
            if (!enemiesManager.isPositionBlocked(newPosition.x, newPosition.y)) {
                position.y += moveAmount;
            }
        }
        if (moveDown) {
            newPosition.set(position.x, position.y - moveAmount);
            if (!enemiesManager.isPositionBlocked(newPosition.x, newPosition.y)) {
                position.y -= moveAmount;
            }
        }
        if (moveLeft) {
            newPosition.set(position.x - moveAmount, position.y);
            if (!enemiesManager.isPositionBlocked(newPosition.x, newPosition.y)) {
                position.x -= moveAmount;
            }
        }
        if (moveRight) {
            newPosition.set(position.x + moveAmount, position.y);
            if (!enemiesManager.isPositionBlocked(newPosition.x, newPosition.y)) {
                position.x += moveAmount;
            }
        }
        player.updateState(delta, moveUp, moveDown, moveLeft, moveRight, isRunning);
        weaponsManager.update(delta);
        weaponsManager.checkPlayerCollision(player);
        if (!canShoot) {
            shootCooldown -= delta;
            if (shootCooldown <= 0) {
                canShoot = true;
            }
        }
        if (canShoot && weaponsManager.getCurrentAmmo(currentWeapon) <= 0) {
            if (settingsMenuController.isAutoReloadEnabled()) {
                weaponsManager.startReload(currentWeapon);
            }
        }
        canShoot = !weaponsManager.isReloading() && weaponsManager.canFire(currentWeapon);
        if (continuousFire && "SMG".equals(currentWeapon)) {
            smgFireTimer -= delta;
            if (smgFireTimer <= 0 && canShoot) {
                handleShot(player.getPosition(), targetPosition, currentWeapon, weaponAngle);
                smgFireTimer = smgFireRate;
            }
        }
    }
    public Vector2 getAutoAimTarget() {
        Preferences prefs = Gdx.app.getPreferences("untildawn_settings");
        int autoAimKey = prefs.getInteger("key_AUTOAIM", Input.Keys.SPACE);
        if (Gdx.input.isKeyPressed(autoAimKey)) {
            System.out.println("Space pressed - looking for targets");
            Vector2 playerPos = player.getPosition();
            Enemy nearest = null;
            float minDist = Float.MAX_VALUE;
            for (Enemy enemy : enemiesManager.getEnemies()) {
                // Skip trees and inactive enemies
                if (!enemy.isActive() || enemy instanceof TreeMonster) continue;
                float dist = playerPos.dst(enemy.getPosition());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = enemy;
                }
            }
            if (nearest != null) {
                System.out.println("Found target at: " + nearest.getPosition());
                autoAimTarget = nearest.getPosition().cpy();
                return autoAimTarget;
            }
            else {
                System.out.println("No valid targets found");
            }
        }
        autoAimTarget = null;
        return null;
    }
    public void handleShot(Vector2 playerPosition, Vector2 targetPosition, String weaponType, float weaponAngle) {
        this.currentWeapon = weaponType;
        if (canShoot && weaponsManager.canFire(weaponType)) {
            switch (weaponType) {
                case "Shotgun":
                    weaponsManager.createShotgunBlast(playerPosition, targetPosition, weaponAngle, player);
                    weaponsManager.fireWeapon(weaponType);
                    canShoot = false;
                    shootCooldown = 0.8f;
                    break;
                case "SMG":
                    weaponsManager.createBullet(playerPosition, targetPosition, weaponType, weaponAngle, player);
                    weaponsManager.fireWeapon(weaponType);
                    canShoot = false;
                    shootCooldown = 0.05f;
                    break;
                default:
                    weaponsManager.createBullet(playerPosition, targetPosition, weaponType, weaponAngle, player);
                    weaponsManager.fireWeapon(weaponType);
                    canShoot = false;
                    shootCooldown = 0.3f;
                    break;
            }
        }
    }

    public void startContinuousFire() {
        if ("SMG".equals(currentWeapon)) {
            continuousFire = true;
        }
    }
    public void stopContinuousFire() {
        continuousFire = false;
    }
    public void updateTargetInfo(Vector2 targetPos, float angle) {
        this.targetPosition.set(targetPos);
        this.weaponAngle = angle;
    }
    public Player getPlayer() {
        return player;
    }
    public MovementManager getMovementManager() {
        return movementManager;
    }
    public IdleManager getIdleManager() {
        return idleManager;
    }
}
