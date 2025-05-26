package untildawn.com.Model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

public class WeaponsManager implements Disposable {
    private Player player;
    private ObjectMap<String, Texture> weaponTextures;
    private Texture bulletHeadTexture; // The hollow circle (0.png)
    private Texture bulletTrailTexture; // The solid circle (1.png)
    private Texture muzzleFlashTexture;
    private Array<Bullet> activeBullets;
    private final float bulletSpeed = 900f; // Speed of bullets
    private final float bulletLifetime = 0.7f; // How long bullets last
    private final int trailLength = 8; // Number of trail segments
    private final ObjectMap<String, Weapons> weapons = new ObjectMap<>();
    private ObjectMap<String, Float> weaponLengths;
    private Array<Bullet> enemyBullets;
    private Array<EffectAnimation> effects = new Array<>();
    public Boolean shouldAdd = false;
    // Effect animation fields
    private TextureRegion[] hitEffectFrames;
    private TextureRegion[] deathEffectFrames;
    private Array<TextureRegion> activeHitEffectFrames = new Array<>();
    private Array<Vector2> hitEffectPositions = new Array<>();
    private Array<Float> hitEffectTimes = new Array<>();
    private Array<TextureRegion> activeDeathEffectFrames = new Array<>();
    private Array<Vector2> deathEffectPositions = new Array<>();
    private Array<Float> deathEffectTimes = new Array<>();
    private float hitEffectDuration = 0.3f;
    private float deathEffectDuration = 0.4f;
    private Texture eyebatProjectileTexture;
    // Muzzle flash properties
    private boolean showMuzzleFlash = false;
    private float muzzleFlashTimer = 0;
    private final float muzzleFlashDuration = 0.08f; // How long the flash stays visible
    private Vector2 muzzleFlashPosition = new Vector2();
    private float muzzleFlashAngle = 0;

    // Ammo and reload management
    private final ObjectMap<String, Integer> maxAmmo = new ObjectMap<>();
    private final ObjectMap<String, Integer> currentAmmo = new ObjectMap<>();
    private final ObjectMap<String, Float> reloadTimes = new ObjectMap<>();
    private boolean isReloading = false;
    private float reloadTimeRemaining = 0f;
    private String reloadingWeapon = null;

    // Reload animation
    private final Texture[] reloadFrames = new Texture[4];
    private final Texture reloadBarBG;
    private final Texture reloadBarFill;
    private int currentReloadFrame = 0;
    private float frameTimer = 0f;
    private final float FRAME_DURATION = 0.15f;

    public WeaponsManager() {
        weaponTextures = new ObjectMap<>();
        loadWeapons();
        loadEffectTextures();
        bulletHeadTexture = new Texture(Gdx.files.internal("Weapons/Particle/0.png"));
        bulletTrailTexture = new Texture(Gdx.files.internal("Weapons/Particle/1.png"));
        muzzleFlashTexture = new Texture(Gdx.files.internal("Weapons/Particle/MuzzleFlash.png"));
        eyebatProjectileTexture = new Texture(Gdx.files.internal("Enemies/Eyebat/EyeMonsterProjecitle.png"));
        activeBullets = new Array<>();

        // Initialize the weapons map
        weapons.put("Revolver", new Weapons("Revolver"));
        weapons.put("Shotgun", new Weapons("Shotgun"));
        weapons.put("SMG", new Weapons("SMG"));

        // Initialize weapon lengths
        weaponLengths = new ObjectMap<>();
        weaponLengths.put("Revolver", 30f);
        weaponLengths.put("Shotgun", 35f);
        weaponLengths.put("SMG", 30f);
        enemyBullets = new Array<>();

        // Initialize ammo counts from Weapons
        for (String weaponType : weapons.keys()) {
            Weapons weapon = weapons.get(weaponType);
            maxAmmo.put(weaponType, weapon.getAmmoCapacity());
            currentAmmo.put(weaponType, weapon.getCurrentAmmo());
            reloadTimes.put(weaponType, weapon.getReloadTime());
        }

        // Load reload animation frames
        for (int i = 0; i < 4; i++) {
            reloadFrames[i] = new Texture(Gdx.files.internal("Weapons/Reload/" + (i+1) + ".png"));
        }

        // Load reload bar textures
        reloadBarBG = new Texture(Gdx.files.internal("Weapons/Reload/Bar0.png"));
        reloadBarFill = new Texture(Gdx.files.internal("Weapons/Reload/Bar1.png"));
    }

    private void loadWeapons() {
        // Load weapon textures
        weaponTextures.put("Revolver", new Texture(Gdx.files.internal("Weapons/Revolver/Still/Revolver.png")));
        weaponTextures.put("Shotgun", new Texture(Gdx.files.internal("Weapons/Shotgun/Still/Shotgun.png")));
        weaponTextures.put("SMG", new Texture(Gdx.files.internal("Weapons/SMG/Still/SMG.png")));
    }

    public void loadEffectTextures() {
        try {
            // Load hit effect frames - try without "assets/" prefix
            hitEffectFrames = new TextureRegion[4];
            for (int i = 0; i < 4; i++) {
                Texture texture = null;
                try {
                    // Try direct path first
                    texture = new Texture(Gdx.files.internal("HitFX/T_FireExplosionSmall_" + i + ".png"));
                } catch (Exception e1) {
                    try {
                        // Try with assets prefix second
                        texture = new Texture(Gdx.files.internal("assets/HitFX/T_FireExplosionSmall_" + i + ".png"));
                    } catch (Exception e2) {
                        System.out.println("Could not find hit effect texture at index " + i);
                        throw e2;
                    }
                }
                hitEffectFrames[i] = new TextureRegion(texture);
            }

            // Load death effect frames - similar approach
            deathEffectFrames = new TextureRegion[4];
            for (int i = 0; i < 4; i++) {
                Texture texture = null;
                try {
                    texture = new Texture(Gdx.files.internal("DeathFX/DeathFX_" + i + ".png"));
                } catch (Exception e1) {
                    try {
                        texture = new Texture(Gdx.files.internal("assets/DeathFX/DeathFX_" + i + ".png"));
                    } catch (Exception e2) {
                        System.out.println("Could not find death effect texture at index " + i);
                        throw e2;
                    }
                }
                deathEffectFrames[i] = new TextureRegion(texture);
            }

            // Initialize arrays for storing active effects
            activeHitEffectFrames = new Array<>();
            hitEffectPositions = new Array<>();
            hitEffectTimes = new Array<>();

            activeDeathEffectFrames = new Array<>();
            deathEffectPositions = new Array<>();
            deathEffectTimes = new Array<>();

            System.out.println("Successfully loaded effect textures");
        } catch (Exception e) {
            Gdx.app.error("WeaponsManager", "Error loading effect textures: " + e.getMessage());
            // Initialize with empty arrays to prevent NPEs
            hitEffectFrames = new TextureRegion[0];
            deathEffectFrames = new TextureRegion[0];
        }
    }
    public void setPlayer(Player player) {
        this.player = player;
    }
    public void addExtraAmmoCapacityToWeapon(String weaponType, int extraAmount) {
        if (weaponType != null) {
            // Get current max ammo value
            int maxAmmoValue = maxAmmo.get(weaponType, 0);

            // Add extra ammo to max capacity
            int newMaxAmmo = maxAmmoValue + extraAmount;

            // Update the max ammo
            maxAmmo.put(weaponType, newMaxAmmo);

            // Update the weapon object if it exists
            Weapons weapon = weapons.get(weaponType);
            if (weapon != null) {
                // If your Weapons class has a setMaxAmmo method, uncomment this:
                // weapon.setMaxAmmo(newMaxAmmo);
            }

            System.out.println("Increased " + weaponType + " max capacity by " +
                extraAmount + ". New max: " + newMaxAmmo);
        }
    }
    public void createBullet(Vector2 playerPos, Vector2 target, String weaponType, float weaponAngle, Player player) {
        // Calculate muzzle position based on weapon type and angle
        float weaponLength = weaponLengths.get(weaponType, 20f); // Default to 20 if weapon not found
        SoundManager.getInstance().play("shot", 0.5f);
        // Calculate the position at the end of weapon barrel
        float muzzleX = playerPos.x + weaponLength * (float)Math.cos(Math.toRadians(weaponAngle));
        float muzzleY = playerPos.y + weaponLength * (float)Math.sin(Math.toRadians(weaponAngle));
        Vector2 muzzlePos = new Vector2(muzzleX, muzzleY);

        // Get the player's bullet spread level
        int spreadLevel = player.getBulletSpreadLevel();

        // Always create at least one bullet (center)
        Vector2 direction = new Vector2(target).sub(muzzlePos).nor();
        Bullet bullet = new Bullet(muzzlePos, direction, bulletSpeed, bulletLifetime, trailLength);
        bullet.setWeaponType(weaponType);
        activeBullets.add(bullet);

        // If player has bullet spread upgrade, create additional bullets
        if (spreadLevel > 0) {
            // Angle between bullets decreases as we add more (to avoid extreme spread)
            float spreadAngle = Math.min(8.0f, 15.0f / (float)Math.sqrt(spreadLevel));

            // Create spreadLevel * 2 additional bullets (evenly distributed on both sides)
            for (int i = 1; i <= spreadLevel; i++) {
                // Create bullet pair at +/- angle
                float angleOffset = spreadAngle * i;

                // Right side bullet
                Vector2 rightDir = new Vector2(direction).rotate(angleOffset);
                Bullet rightBullet = new Bullet(muzzlePos, rightDir, bulletSpeed, bulletLifetime, trailLength);
                rightBullet.setWeaponType(weaponType);
                activeBullets.add(rightBullet);

                // Left side bullet
                Vector2 leftDir = new Vector2(direction).rotate(-angleOffset);
                Bullet leftBullet = new Bullet(muzzlePos, leftDir, bulletSpeed, bulletLifetime, trailLength);
                leftBullet.setWeaponType(weaponType);
                activeBullets.add(leftBullet);
            }
        }

        // Create muzzle flash at the barrel position
        showMuzzleFlash = true;
        muzzleFlashTimer = 0;
        muzzleFlashPosition.set(muzzlePos);
        muzzleFlashAngle = weaponAngle;
    }
    public void increaseMaxAmmo(String weaponType, int amount) {
        if (weaponType == null || amount <= 0) {
            return;
        }

        // Update max ammo in the maxAmmo map
        int currentMaxAmmo = maxAmmo.get(weaponType, 0);
        maxAmmo.put(weaponType, currentMaxAmmo + amount);

        // IMPORTANT: Also increase current ammo by the same amount
        // so the player can use the new capacity immediately
            int currentAmmoValue = currentAmmo.get(weaponType, 0);
        currentAmmo.put(weaponType, currentAmmoValue + amount);
        // Update the Weapons object to keep them in sync
        Weapons weapon = weapons.get(weaponType);
        if (weapon != null) {
            weapon.increaseAmmoCapacity(amount);
        }
    }
    public TextureRegion getWeaponTexture(String weaponName) {
        if (weaponTextures.containsKey(weaponName)) {
            return new TextureRegion(weaponTextures.get(weaponName));
        }
        // Return default weapon if not found
        return new TextureRegion(weaponTextures.get("Revolver"));
    }

    public void update(float delta) {
        // Update all bullets and remove dead ones
        for (int i = activeBullets.size - 1; i >= 0; i--) {
            Bullet bullet = activeBullets.get(i);
            bullet.update(delta);

            if (!bullet.isAlive()) {
                activeBullets.removeIndex(i);
            }
        }

        // Update muzzle flash
        if (showMuzzleFlash) {
            muzzleFlashTimer += delta;
            if (muzzleFlashTimer >= muzzleFlashDuration) {
                showMuzzleFlash = false;
            }
        }

        // Update reload progress
        if (isReloading) {
            reloadTimeRemaining -= delta;

            // Update animation frame
            frameTimer += delta;
            if (frameTimer >= FRAME_DURATION) {
                frameTimer = 0;
                currentReloadFrame = (currentReloadFrame + 1) % 4;
            }

            if (reloadTimeRemaining <= 0) {
                completeReload();
            }
        }

        // Update visual effects
        updateEffects(delta);
    }

    public void checkEnemyCollisions(Array<Enemy> enemies, Player player) {
        if (enemies == null || enemies.size == 0) return;

        for (int i = activeBullets.size - 1; i >= 0; i--) {
            Bullet bullet = activeBullets.get(i);
            if (bullet == null || !bullet.isAlive()) continue;

            try {
                // Only check player bullets (not enemy ones)
                if (!bullet.isEnemy()) {
                    // Get weapon info once outside the enemy loop
                    String weaponType = bullet.getWeaponType();
                    Weapons weapon = weapons.get(weaponType != null ? weaponType : "Revolver");

                    if (weapon == null) {
                        // Fallback for unknown weapon type
                        continue;
                    }

                    int damage = (int)(weapon.getDamage() * player.getDamageMultiplier());

                    for (int j = 0; j < enemies.size; j++) {
                        Enemy enemy = enemies.get(j);
                        if (enemy == null || !enemy.isActive()) continue;

                        float distance = Vector2.dst(
                            enemy.getPosition().x, enemy.getPosition().y,
                            bullet.getPosition().x, bullet.getPosition().y
                        );

                        float collisionRadius = enemy.getCollisionRadius();

                        if (distance < collisionRadius && !bullet.hasHitEnemy(enemy)) {
                            // Apply damage and effects
                            SoundManager.getInstance().play("impact", 0.5f);
                            enemy.takeDamage(damage);
                            enemy.startHitFlash();

                            // Create hit effect safely
                            try {
                                Vector2 hitPos = new Vector2(bullet.getPosition());
                                createHitEffect(hitPos);

                                // Check if enemy died from this hit
                                if (!enemy.isActive()) {
                                    createDeathEffect(new Vector2(enemy.getPosition()));
                                    player.incrementKillCount();
                                }
                            } catch (Exception e) {
                                Gdx.app.error("WeaponsManager", "Error creating hit effect: " + e.getMessage());
                            }

                            // Mark this enemy as hit by this bullet
                            bullet.addHitEnemy(enemy);
                        }
                    }
                }
            } catch (Exception e) {
                Gdx.app.error("WeaponsManager", "Error in collision detection: " + e.getMessage());
            }
        }
    }

    private void createHitEffect(Vector2 position) {
        try {
            // Make sure necessary textures and arrays are initialized
            if (activeHitEffectFrames == null) {
                activeHitEffectFrames = new Array<>();
            }
            if (hitEffectPositions == null) {
                hitEffectPositions = new Array<>();
            }
            if (hitEffectTimes == null) {
                hitEffectTimes = new Array<>();
            }

            // Make sure we have frames to display
            if (hitEffectFrames != null && hitEffectFrames.length > 0 && hitEffectFrames[0] != null) {
                activeHitEffectFrames.add(hitEffectFrames[0]);
                hitEffectPositions.add(new Vector2(position));
                hitEffectTimes.add(0f);
            }
        } catch (Exception e) {
            Gdx.app.error("WeaponsManager", "Error in createHitEffect: " + e.getMessage());
        }
    }

    private void createDeathEffect(Vector2 position) {
        try {
            if (activeDeathEffectFrames == null) {
                activeDeathEffectFrames = new Array<>();
            }
            if (deathEffectPositions == null) {
                deathEffectPositions = new Array<>();
            }
            if (deathEffectTimes == null) {
                deathEffectTimes = new Array<>();
            }

            if (deathEffectFrames != null && deathEffectFrames.length > 0 && deathEffectFrames[0] != null) {
                activeDeathEffectFrames.add(deathEffectFrames[0]);
                deathEffectPositions.add(new Vector2(position));
                deathEffectTimes.add(0f);
            }
        } catch (Exception e) {
            Gdx.app.error("WeaponsManager", "Error in createDeathEffect: " + e.getMessage());
        }
    }

    public void updateEffects(float delta) {
        // Update hit effects
        for (int i = activeHitEffectFrames.size - 1; i >= 0; i--) {
            float time = hitEffectTimes.get(i) + delta;
            hitEffectTimes.set(i, time);

            // Update animation frame
            int frameIndex = (int)(time / (hitEffectDuration/4));
            if (frameIndex >= hitEffectFrames.length) {
                activeHitEffectFrames.removeIndex(i);
                hitEffectPositions.removeIndex(i);
                hitEffectTimes.removeIndex(i);
            } else {
                activeHitEffectFrames.set(i, hitEffectFrames[frameIndex]);
            }
        }

        // Update death effects
        for (int i = activeDeathEffectFrames.size - 1; i >= 0; i--) {
            float time = deathEffectTimes.get(i) + delta;
            deathEffectTimes.set(i, time);

            // Update animation frame
            int frameIndex = (int)(time / (deathEffectDuration/4));
            if (frameIndex >= deathEffectFrames.length) {
                activeDeathEffectFrames.removeIndex(i);
                deathEffectPositions.removeIndex(i);
                deathEffectTimes.removeIndex(i);
            } else {
                activeDeathEffectFrames.set(i, deathEffectFrames[frameIndex]);
            }
        }
    }

    public void renderEffects(SpriteBatch batch) {
        // Render hit effects
        for (int i = 0; i < activeHitEffectFrames.size; i++) {
            TextureRegion region = activeHitEffectFrames.get(i);
            Vector2 pos = hitEffectPositions.get(i);
            float width = region.getRegionWidth() * 0.7f;
            float height = region.getRegionHeight() * 0.7f;
            batch.draw(region, pos.x - width/2, pos.y - height/2, width, height);
        }

        // Render death effects
        for (int i = 0; i < activeDeathEffectFrames.size; i++) {
            TextureRegion region = activeDeathEffectFrames.get(i);
            Vector2 pos = deathEffectPositions.get(i);
            float width = region.getRegionWidth();
            float height = region.getRegionHeight();
            batch.draw(region, pos.x - width/2, pos.y - height/2, width, height);
        }
    }

    public void renderBullets(SpriteBatch batch) {
        for (Bullet bullet : activeBullets) {
            if (bullet.isEnemy()) {
                // Draw eyebat projectile
                float size = eyebatProjectileTexture.getWidth() * 0.8f;
                batch.setColor(1, 1, 1, 1);
                batch.draw(eyebatProjectileTexture,
                    bullet.getPosition().x - size/2,
                    bullet.getPosition().y - size/2,
                    size, size);
            } else {
                // Draw the trail (solid circles) first so they appear behind the head
                Array<Vector2> trail = bullet.getTrailPositions();
                for (int i = 0; i < trail.size - 1; i++) { // Skip the last one (head position)
                    Vector2 pos = trail.get(i);
                    float alpha = 0.8f * (float)i / trail.size; // Fade in as we get closer to the head
                    float scale = 0.6f + (0.4f * i / trail.size); // Get bigger toward the head

                    batch.setColor(1, 1, 1, alpha);
                    float trailSize = bulletTrailTexture.getWidth() * scale;
                    batch.draw(bulletTrailTexture,
                        pos.x - trailSize/2,
                        pos.y - trailSize/2,
                        trailSize,
                        trailSize);
                }

                // Draw the head (hollow circle)
                batch.setColor(1, 1, 1, 1); // Reset color
                float headSize = bulletHeadTexture.getWidth() * 0.8f; // Slightly smaller head
                batch.draw(bulletHeadTexture,
                    bullet.getPosition().x - headSize/2,
                    bullet.getPosition().y - headSize/2,
                    headSize,
                    headSize);
            }
        }
        batch.setColor(1, 1, 1, 1); // Reset color
    }

    public void renderMuzzleFlash(SpriteBatch batch) {
        if (showMuzzleFlash && muzzleFlashTexture != null) {
            float size = muzzleFlashTexture.getWidth() * 1.5f;

            batch.draw(
                muzzleFlashTexture,
                muzzleFlashPosition.x - size/2,
                muzzleFlashPosition.y - size/2,
                size/2, size/2,
                size, size,
                1, 1,
                muzzleFlashAngle,
                0, 0,
                muzzleFlashTexture.getWidth(),
                muzzleFlashTexture.getHeight(),
                false, false
            );
        }
    }

    public void createShotgunBlast(Vector2 playerPos, Vector2 target, float weaponAngle, Player player) {
        // Save the original angle and calculate the muzzle position only once
        float originalAngle = weaponAngle;

        // Calculate muzzle position based on the original angle
        float weaponLength = weaponLengths.get("Shotgun", 35f);
        float muzzleX = playerPos.x + weaponLength * (float)Math.cos(Math.toRadians(originalAngle));
        float muzzleY = playerPos.y + weaponLength * (float)Math.sin(Math.toRadians(originalAngle));
        Vector2 muzzlePos = new Vector2(muzzleX, muzzleY);

        // Fire bullets without creating a muzzle flash (we'll do that once at the end)
        boolean originalMuzzleState = showMuzzleFlash;
        showMuzzleFlash = false;

        // Create bullets with spread
        float spreadAngle = 10f;

        // Center bullet
        createBullet(playerPos, target, "Shotgun", weaponAngle, player);

        // Create spread bullets
        for (int i = 1; i <= 3; i++) {
            float angleOffset = (i % 2 == 0) ? (i/2 * spreadAngle) : (-((i+1)/2) * spreadAngle);
            float newAngle = weaponAngle + angleOffset;

            Vector2 spreadTarget = new Vector2();
            spreadTarget.x = playerPos.x + 1000 * (float)Math.cos(Math.toRadians(newAngle));
            spreadTarget.y = playerPos.y + 1000 * (float)Math.sin(Math.toRadians(newAngle));

            createBullet(playerPos, spreadTarget, "Shotgun", newAngle, player);
        }

        // Create a single muzzle flash with the original angle and position
        showMuzzleFlash = true;
        muzzleFlashTimer = 0;
        muzzleFlashPosition.set(muzzlePos);
        muzzleFlashAngle = originalAngle;
    }

    public boolean canFire(String weaponType) {
        // Check current ammo directly
        int ammo = currentAmmo.get(weaponType, 0);
        boolean canFire = ammo > 0 && !isReloading;


        return canFire;
    }

    public void fireWeapon(String weaponType) {
        int ammo = currentAmmo.get(weaponType, 0);
        if (ammo > 0) {
            currentAmmo.put(weaponType, ammo - 1);

            // Get and update the Weapons object
            Weapons weapon = weapons.get(weaponType);
            if (weapon != null) {
                // MAKE SURE weapon's internal state matches our state
                weapon.setCurrentAmmo(currentAmmo.get(weaponType, 0));
            }
        }
    }

    public void startReload(String weaponType) {
        if (weaponType == null || isReloading) {
            return;
        }
        if (currentAmmo.get(weaponType, 0) < maxAmmo.get(weaponType, 0)) {
            isReloading = true;
            SoundManager.getInstance().play("reload", 0.6f);
            reloadingWeapon = weaponType;
            reloadTimeRemaining = reloadTimes.get(weaponType, 1.5f);
            currentReloadFrame = 0;
            frameTimer = 0f;
        }
    }

    private void completeReload() {
        if (reloadingWeapon != null) {
            // Get the original max ammo value for this weapon
            int maxAmmoValue = maxAmmo.get(reloadingWeapon);
            int finalAmmo = maxAmmoValue;
            if (shouldAdd){
                finalAmmo += 5;
            }
            // Update both the map and the weapon object with the same final value
            currentAmmo.put(reloadingWeapon, finalAmmo);

            // Update the weapon object with the same final ammo
            Weapons weapon = weapons.get(reloadingWeapon);
            if (weapon != null) {
                weapon.setCurrentAmmo(finalAmmo);
            }
        }
        isReloading = false;
        reloadingWeapon = null;
    }
    public void addExtraAmmoToCurrentWeapon(String weaponType, int extraAmount) {
        if (weaponType != null) {
            boolean shouldAdd = true;
            // Get current ammo values
            int currentAmmoValue = currentAmmo.get(weaponType, 0);
            int maxAmmoValue = maxAmmo.get(weaponType, 0);

            // Add extra ammo to current amount
            int newAmmo = currentAmmoValue + extraAmount;

            // Update the current ammo
            currentAmmo.put(weaponType, newAmmo);

            // Update the weapon object
            Weapons weapon = weapons.get(weaponType);
            if (weapon != null) {
                weapon.setCurrentAmmo(newAmmo);
                System.out.println("Added " + extraAmount + " extra bullets to " + weaponType +
                    ": " + newAmmo + "/" + maxAmmoValue);
            }
        }
    }
    public void createEnemyBullet(Vector2 position, Vector2 target) {
        // Create direction vector
        Vector2 direction = new Vector2(target).sub(position).nor();

        float speed = 300f;  // Speed for enemy bullets
        float lifetime = 1.5f;

        // Create bullet
        Bullet bullet = new Bullet(position, direction, speed, lifetime, trailLength);
        bullet.setIsEnemy(true);  // Mark as enemy bullet
        activeBullets.add(bullet);
    }

    public void checkPlayerCollision(Player player) {
        for (int i = activeBullets.size - 1; i >= 0; i--) {
            Bullet bullet = activeBullets.get(i);
            if (bullet.isAlive() && bullet.isEnemy()) {
                // Simple collision check
                float distance = player.getPosition().dst(bullet.getPosition());
                float collisionRadius = 15; // Adjust for better hit detection

                if (distance < collisionRadius) {
                    player.takeDamage(1); // Damage amount
                    player.setHit(true); // Add this line to trigger hit animation
                    bullet.kill();
                    // Optional: Add hit effect/sound here
                }
            }
        }
    }

    public void renderReloadUI(SpriteBatch batch, Vector2 playerPosition) {
        if (!isReloading) return;

        float totalReloadTime = reloadTimes.get(reloadingWeapon, 1.5f);
        float progress = 1 - (reloadTimeRemaining / totalReloadTime);

        // Draw reload animation
        Texture currentFrame = reloadFrames[currentReloadFrame];
        float frameX = playerPosition.x + 5;
        float frameY = playerPosition.y - currentFrame.getHeight() / 2 - 5;
        batch.draw(currentFrame, frameX, frameY);

        // Draw reload progress bar
        float barX = playerPosition.x - currentFrame.getWidth() * 2;
        float barY = playerPosition.y + 25;

        // Draw background bar
        batch.draw(reloadBarBG, barX, barY);

        // Calculate marker position based on progress
        float markerWidth = 3f;
        float markerX = barX + (progress * (reloadBarBG.getWidth() - markerWidth));

        // Draw the marker (vertical line) using a simpler draw method
        batch.draw(reloadBarFill,
            markerX, barY,
            markerWidth, reloadBarFill.getHeight());
    }

    public int getCurrentAmmo(String weaponType) {
        if (weaponType == null) {
            return 0; // Safe default for null weapon
        }
        return currentAmmo.get(weaponType, 0);
    }

    public int getMaxAmmo(String weaponType) {
        if (weaponType == null) {
            return 0; // Safe default for null weapon
        }
        return maxAmmo.get(weaponType, 0);
    }

    public boolean isReloading() {
        return isReloading;
    }

    @Override
    public void dispose() {
        for (Texture texture : weaponTextures.values()) {
            texture.dispose();
        }
        if (bulletHeadTexture != null) bulletHeadTexture.dispose();
        if (bulletTrailTexture != null) bulletTrailTexture.dispose();
        if (muzzleFlashTexture != null) muzzleFlashTexture.dispose();

        for (Texture texture : reloadFrames) {
            if (texture != null) texture.dispose();
        }

        // Dispose effect textures
        if (hitEffectFrames != null) {
            for (TextureRegion region : hitEffectFrames) {
                if (region.getTexture() != null) region.getTexture().dispose();
            }
        }

        if (deathEffectFrames != null) {
            for (TextureRegion region : deathEffectFrames) {
                if (region.getTexture() != null) region.getTexture().dispose();
            }
        }
        if (eyebatProjectileTexture != null) eyebatProjectileTexture.dispose();
        reloadBarBG.dispose();
        reloadBarFill.dispose();
        enemyBullets.clear();
    }
}
