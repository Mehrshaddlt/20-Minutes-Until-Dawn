package untildawn.com.Model;

import com.badlogic.gdx.math.Vector2;

public class Player {
    private User user;
    private final String characterName;
    private final Vector2 position;
    private float baseWalkSpeed = 100f;
    private float baseRunSpeed = 200f;
    public float walkSpeed;
    public float runSpeed;
    private float speed;
    private int level = 1;
    private int currentXP = 0;
    private int xpToNextLevel = 20;
    private WeaponsManager weaponsManager;
    private int maxHealth;
    private int currentHealth;
    private LevelUpAnimation levelUpAnimation;
    public int lastLevel;
    private float damageMultiplier = 1.0f;
    private float speedMultiplier = 1.0f;
    private int extraProjectiles = 0;
    private int bulletSpreadLevel = 0;
    private int killCount = 0;
    private boolean vitalityActive = false;
    private MovementState state;
    private Direction direction;
    private float stateTime;
    private boolean extraBulletActive = false;
    private String currentWeapon = "Revolver";
    private boolean ammoIncreaseActive = false;
    private float ammoIncreaseTimer = 0f;
    private final float AMMO_INCREASE_DURATION = 3f;
    private boolean hit = false;
    public enum MovementState {
        IDLE,
        WALKING,
        RUNNING
    }
    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }
    public Player(String characterName, float x, float y, User user) {
        this.characterName = characterName;
        this.position = new Vector2(x, y);
        this.user = user;
        CharacterStats.Stats stats = CharacterStats.getStats(characterName);
        this.maxHealth = stats.getMaxHealth();
        this.currentHealth = maxHealth;
        float speedMultiplier = stats.getSpeedMultiplier() / 4f;
        this.walkSpeed = baseWalkSpeed * speedMultiplier;
        this.runSpeed = baseRunSpeed * speedMultiplier;
        this.speed = walkSpeed;
        levelUpAnimation = new LevelUpAnimation();
        lastLevel = level;
        this.state = MovementState.IDLE;
        this.direction = Direction.DOWN;
        this.stateTime = 0f;
        initLevelUpAnimation();
        this.weaponsManager = new WeaponsManager();
        weaponsManager.setPlayer(this);
        this.currentWeapon = "Revolver";
    }
    public LevelUpAnimation getLevelUpAnimation() {
        return levelUpAnimation;
    }
    public void initLevelUpAnimation() {
        levelUpAnimation = new LevelUpAnimation();
        lastLevel = level;
    }
    public void updateState(float delta, boolean moveUp, boolean moveDown, boolean moveLeft, boolean moveRight, boolean isRunning) {
        stateTime += delta;
        boolean isMoving = moveUp || moveDown || moveLeft || moveRight;
        if (!isMoving) {
            state = MovementState.IDLE;
            SoundManager.getInstance().stopLoopingSound("footsteps");
        }
        else if (isRunning) {
            state = MovementState.RUNNING;
            speed = runSpeed;
            SoundManager.getInstance().startLoopingSound("footsteps", 0.2f);
        }
        else {
            state = MovementState.WALKING;
            speed = walkSpeed;
            SoundManager.getInstance().startLoopingSound("footsteps", 0.1f);
        }
        if (moveLeft) {
            direction = Direction.LEFT;
        }
        else if (moveRight) {
            direction = Direction.RIGHT;
        }
        else if (moveUp) {
            direction = Direction.UP;
        }
        else if (moveDown) {
            direction = Direction.DOWN;
        }
        if (levelUpAnimation != null && levelUpAnimation.isActive()) {
            levelUpAnimation.update(delta, position);
            System.out.println("Updating animation, time: " + levelUpAnimation.getStateTime());
        }

    }
    public boolean isAmmoIncreaseActive() {
        return ammoIncreaseActive;
    }
    public void setHit(boolean hit) {
        this.hit = hit;
    }
    public boolean isHit() {
        boolean wasHit = hit;
        hit = false;
        return wasHit;
    }
    public void update(float delta, boolean moveUp, boolean moveDown, boolean moveLeft, boolean moveRight, boolean isRunning) {
        stateTime += delta;
        boolean isMoving = moveUp || moveDown || moveLeft || moveRight;
        if (!isMoving) {
            state = MovementState.IDLE;
            SoundManager.getInstance().stopLoopingSound("footsteps");
            System.out.println("Stopped footsteps");
        }
        else if (isRunning) {
            state = MovementState.RUNNING;
            speed = runSpeed;
            SoundManager.getInstance().startLoopingSound("footsteps", 0.4f);
            System.out.println("started footsteps (running)");
        }
        else {
            state = MovementState.WALKING;
            speed = walkSpeed;
            SoundManager.getInstance().startLoopingSound("footsteps", 0.3f);
            System.out.println("started footsteps (walking)");
        }
        if (moveLeft) {
            direction = Direction.LEFT;
        }
        else if (moveRight) {
            direction = Direction.RIGHT;
        }
        else if (moveUp) {
            direction = Direction.UP;
        }
        else if (moveDown) {
            direction = Direction.DOWN;
        }
        float moveAmount = speed * delta;
        if (moveUp) position.y += moveAmount;
        if (moveDown) position.y -= moveAmount;
        if (moveLeft) position.x -= moveAmount;
        if (moveRight) position.x += moveAmount;
        if (ammoIncreaseActive) {
            ammoIncreaseTimer -= delta;
            if (ammoIncreaseTimer <= 0) {
                ammoIncreaseActive = false;
            }
        }
        if (levelUpAnimation != null) {
            levelUpAnimation.update(delta, new Vector2(position.x, position.y));
        }
        if (hit) {
            hit = false;
        }
    }
    public void takeDamage(int amount) {
        currentHealth = Math.max(0, currentHealth - amount);
        setHit(true);
    }
    public void activateAmmoIncrease() {
        ammoIncreaseActive = true;
        ammoIncreaseTimer = AMMO_INCREASE_DURATION;
    }
    public void heal(int amount) {
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }
    public void gainXP(int amount) {
        currentXP += amount;
        int previousLevel = level;
        SoundManager.getInstance().play("xp_pickup", 0.5f);
        checkLevelUp();
        if (level > previousLevel) {
            if (levelUpAnimation == null) {
                levelUpAnimation = new LevelUpAnimation();
            }
            SoundManager.getInstance().play("levelup", 0.7f);
            levelUpAnimation.start(position);
        }
    }
    private void checkLevelUp() {
        while (currentXP >= xpToNextLevel) {
            level++;
            currentXP -= xpToNextLevel;
            xpToNextLevel = 20 * level;
        }
    }
    public int getLevel() {
        return level;
    }
    public int getCurrentXP() {
        return currentXP;
    }
    public int getXpToNextLevel() {
        return xpToNextLevel;
    }
    public float getLevelProgress() {
        return (float)currentXP / xpToNextLevel;
    }
    public boolean isAlive() {
        return currentHealth > 0;
    }
    public void addMaxHearts(int count) {
        this.maxHealth += count;
    }
    public void restoreFullHealth() {
        this.currentHealth = this.maxHealth;
    }
    public Vector2 getPosition() {
        return position;
    }
    public void setPosition(float x, float y) {
        position.set(x, y);
    }
    public MovementState getState() {
        return state;
    }
    public Direction getDirection() {
        return direction;
    }
    public String getCharacterName() {
        return characterName;
    }
    public float getStateTime() {
        return stateTime;
    }
    public int getCurrentHealth() {
        return currentHealth;
    }
    public int getMaxHealth() {
        return maxHealth;
    }
    public void setDamageMultiplier(float multiplier) {
        this.damageMultiplier = multiplier;
    }
    public float getDamageMultiplier() {
        return damageMultiplier;
    }
    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = multiplier;
        this.walkSpeed = baseWalkSpeed * speedMultiplier;
        this.runSpeed = baseRunSpeed * speedMultiplier;
    }
    public void resetSpeedMultiplier() {
        this.speedMultiplier = 1.0f;
        this.walkSpeed = baseWalkSpeed * speedMultiplier;
        this.runSpeed = baseRunSpeed * speedMultiplier;
    }
    public float getSpeedMultiplier() {
        return speedMultiplier;
    }
    public void addExtraProjectile(int amount) {
        extraProjectiles += amount;
    }
    public int getExtraProjectiles() {
        return extraProjectiles;
    }
    public String getCurrentWeapon() {
        return currentWeapon;
    }
    public void setCurrentWeapon(String weapon) {
        this.currentWeapon = weapon;
    }
    public int getKillCount() {
        return killCount;
    }
    public User getUser() {
        return user;
    }
    public int getBulletSpreadLevel() {
        return bulletSpreadLevel;
    }
    public void activateVitality() {
        this.vitalityActive = true;
    }
    public boolean isVitalityActive() {
        return vitalityActive;
    }
    public void incrementBulletSpreadLevel() {
        bulletSpreadLevel++;
    }
    public void incrementKillCount() {
        killCount++;
    }
}
