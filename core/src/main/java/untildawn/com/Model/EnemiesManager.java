package untildawn.com.Model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

public class EnemiesManager {
    private Array<Enemy> enemies;
    private TextureRegion[] treeTextures;
    private TextureRegion[] brainTextures;
    private TextureRegion[] eyebatTextures;
    private Random random;
    private float gameTime = 0;
    private float lastBrainSpawnTime = 0;
    private float lastEyebatSpawnTime = 0;
    private Array<XPDrop> xpDrops;
    private Texture xpDropTexture;

    private final int MAP_WIDTH;
    private final int MAP_HEIGHT;
    private WeaponsManager weaponsManager;

    public EnemiesManager(int mapWidth, int mapHeight, WeaponsManager weaponsManager) {
        this.MAP_WIDTH = mapWidth;
        this.MAP_HEIGHT = mapHeight;
        this.weaponsManager = weaponsManager;
        this.xpDrops = new Array<>();
        this.xpDropTexture = new Texture(Gdx.files.internal("assets/Level/T_SpawnedBug_Em.png"));
        enemies = new Array<>();
        random = new Random();
        loadTextures();
    }

    public boolean isPositionBlocked(float x, float y) {
        for (Enemy enemy : enemies) {
            if (enemy.isActive() && enemy instanceof TreeMonster) {
                TreeMonster tree = (TreeMonster) enemy;
                if (tree.collidesWithPoint(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void loadTextures() {
        // Load tree textures (3 states)
        treeTextures = new TextureRegion[3];
        for (int i = 0; i < 3; i++) {
            Texture treeTexture = new Texture(Gdx.files.internal("assets/Enemies/Tree/T_TreeMonster_" + i + ".png"));
            treeTextures[i] = new TextureRegion(treeTexture);
        }

        // Load brain monster textures (7 frames)
        brainTextures = new TextureRegion[7];
        for (int i = 0; i < 7; i++) {
            Texture brainTexture = new Texture(Gdx.files.internal("assets/Enemies/Brain/BrainMonster_" + i + ".png"));
            brainTextures[i] = new TextureRegion(brainTexture);
        }

        // Load eyebat monster textures (5 frames)
        eyebatTextures = new TextureRegion[5];
        for (int i = 0; i < 5; i++) {
            Texture eyebatTexture = new Texture(Gdx.files.internal("assets/Enemies/Eyebat/T_EyeBat_" + i + ".png"));
            eyebatTextures[i] = new TextureRegion(eyebatTexture);
        }
    }

    public void spawnInitialTrees(int count) {
        // Clear existing trees if any
        enemies.clear();

        // Use the full map dimensions for spawning
        float minDistanceBetweenTrees = 150f;
        float edgeBuffer = 50f; // Buffer from map edges
        float playerSafeRadius = 200f; // Safe zone around player spawn
        float centerX = MAP_WIDTH / 2;
        float centerY = MAP_HEIGHT / 2;

        Random random = new Random();

        // Create a grid system for more even distribution
        int gridSize = 200; // Adjust based on your map size
        int gridCols = (int)(MAP_WIDTH / gridSize);
        int gridRows = (int)(MAP_HEIGHT / gridSize);

        // Try to distribute trees across the grid cells
        for (int i = 0; i < count; i++) {
            boolean validPositionFound = false;
            int attempts = 0;
            float x = 0, y = 0;

            while (!validPositionFound && attempts < 15) {
                // Choose a random grid cell
                int gridCol = random.nextInt(Math.max(1, gridCols));
                int gridRow = random.nextInt(Math.max(1, gridRows));

                // Generate position within that cell with some randomness
                x = (gridCol * gridSize) + random.nextFloat() * gridSize;
                y = (gridRow * gridSize) + random.nextFloat() * gridSize;

                // Check if position is valid (not too close to edges, other trees, or player spawn)
                if (x < edgeBuffer || x > MAP_WIDTH - edgeBuffer ||
                    y < edgeBuffer || y > MAP_HEIGHT - edgeBuffer) {
                    attempts++;
                    continue;
                }

                // Check distance from player spawn point
                float dx = x - centerX;
                float dy = y - centerY;
                if (dx * dx + dy * dy < playerSafeRadius * playerSafeRadius) {
                    attempts++;
                    continue;
                }

                // Check distance from other trees
                boolean tooClose = false;
                for (Enemy enemy : enemies) {
                    if (enemy instanceof TreeMonster) {
                        dx = enemy.getPosition().x - x;
                        dy = enemy.getPosition().y - y;
                        if (dx * dx + dy * dy < minDistanceBetweenTrees * minDistanceBetweenTrees) {
                            tooClose = true;
                            break;
                        }
                    }
                }

                if (!tooClose) {
                    validPositionFound = true;
                }

                attempts++;
            }

            if (validPositionFound) {
                TreeMonster tree = new TreeMonster(x, y, treeTextures);
                enemies.add(tree);
            }
        }
    }

    public void update(float delta, Player player, float totalGameTime) {
        gameTime += delta;

        // Spawn brain monsters based on time formula (i/30)
        int spawnCount = (int)(gameTime / 30);
        if (spawnCount > 0 && lastBrainSpawnTime + 3 <= gameTime) { // Every 3 seconds
            lastBrainSpawnTime = gameTime;
            for (int i = 0; i < spawnCount; i++) {
                spawnBrainMonster(player);
            }
        }
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            if (!enemy.isActive() && !enemy.hasAwardedXP()) {
                // Spawn XP drop at enemy position
                if (enemy instanceof BrainMonster) {
                    SoundManager.getInstance().play("brainmonsterDeath");
                } else if (enemy instanceof EyebatMonster) {
                    SoundManager.getInstance().play("bateyeDeath");
                }
                spawnXPDrop(enemy.getPosition().x, enemy.getPosition().y);
                enemy.setHasAwardedXP(true); // Mark XP drop as spawned
            }
        }
        // Spawn eyebat monsters only after 1/4 of total game time has passed
        // with formula (4i - t + 30)/30 every 10 seconds
        if (gameTime >= totalGameTime / 4 && lastEyebatSpawnTime + 10 <= gameTime) {
            lastEyebatSpawnTime = gameTime;

            // Calculate spawn count using the specified formula
            int eyebatSpawnCount = Math.max(0, (int)((4 * gameTime - totalGameTime + 30) / 30));
            eyebatSpawnCount = Math.min(eyebatSpawnCount, 2); // Cap at 2 to prevent overwhelming the player

            for (int i = 0; i < eyebatSpawnCount; i++) {
                spawnEyebatMonster(player);
            }
        }

        // Update existing enemies
        for (Enemy enemy : enemies) {
            if (enemy.isActive()) {
                enemy.update(delta, player);
            }
        }
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
        }
        updateXPDrops(player);
    }
    private void spawnXPDrop(float x, float y) {
        xpDrops.add(new XPDrop(x, y, xpDropTexture));
    }
    private void updateXPDrops(Player player) {
        // Get player collision circle
        Circle playerCircle = new Circle(player.getPosition(), 10); // Adjust radius as needed

        // Check for collision with XP drops
        for (int i = xpDrops.size - 1; i >= 0; i--) {
            XPDrop drop = xpDrops.get(i);
            if (drop.isActive() && playerCircle.overlaps(drop.getCollisionCircle())) {
                // Player collected the XP drop
                player.gainXP(drop.getXpValue());
                drop.collect();
                xpDrops.removeIndex(i);
            }
        }
    }
    private void spawnBrainMonster(Player player) {
        float spawnX, spawnY;
        float buffer = 20f; // Small buffer from the edge

        // Direction from which to spawn (0-3: top, right, bottom, left)
        int direction = random.nextInt(4);
        switch(direction) {
            case 0: // Top edge
                spawnX = random.nextFloat() * MAP_WIDTH;
                spawnY = MAP_HEIGHT - buffer;
                break;
            case 1: // Right edge
                spawnX = MAP_WIDTH - buffer;
                spawnY = random.nextFloat() * MAP_HEIGHT;
                break;
            case 2: // Bottom edge
                spawnX = random.nextFloat() * MAP_WIDTH;
                spawnY = buffer;
                break;
            default: // Left edge
                spawnX = buffer;
                spawnY = random.nextFloat() * MAP_HEIGHT;
                break;
        }

        // Create and add brain monster
        BrainMonster brain = new BrainMonster(spawnX, spawnY, brainTextures);
        enemies.add(brain);
    }

    private void spawnEyebatMonster(Player player) {
        float spawnX, spawnY;
        float buffer = 20f; // Small buffer from the edge

        // Direction from which to spawn (0-3: top, right, bottom, left)
        int direction = random.nextInt(4);
        switch(direction) {
            case 0: // Top edge
                spawnX = random.nextFloat() * MAP_WIDTH;
                spawnY = MAP_HEIGHT - buffer;
                break;
            case 1: // Right edge
                spawnX = MAP_WIDTH - buffer;
                spawnY = random.nextFloat() * MAP_HEIGHT;
                break;
            case 2: // Bottom edge
                spawnX = random.nextFloat() * MAP_WIDTH;
                spawnY = buffer;
                break;
            default: // Left edge
                spawnX = buffer;
                spawnY = random.nextFloat() * MAP_HEIGHT;
                break;
        }

        // Create and add eyebat monster
        EyebatMonster eyebat = new EyebatMonster(spawnX, spawnY, eyebatTextures, weaponsManager);
        enemies.add(eyebat);
    }

    public void render(SpriteBatch batch) {
        for (Enemy enemy : enemies) {
            if (enemy.isActive()) {
                enemy.render(batch);
            }
        }
        for (XPDrop drop : xpDrops) {
            if (drop.isActive()) {
                drop.render(batch);
            }
        }
    }
    public void clearMobileEnemies() {
        // Create a new list to hold only trees
        Array<Enemy> trees = new Array<>();

        // Separate trees from mobile enemies
        for (Enemy enemy : enemies) {
            if (enemy instanceof TreeMonster) {
                trees.add(enemy);
            }
        }

        // Clear all enemies then add back just the trees
        enemies.clear();
        enemies.addAll(trees);
    }
    public Array<Enemy> getEnemies() {
        return enemies;
    }
    public void clearAllEnemies() {
        enemies.clear();
        // If you have other enemy collections, clear them too
    }
    public void dispose() {
        // Dispose of textures
        if (treeTextures != null) {
            for (TextureRegion region : treeTextures) {
                if (region != null && region.getTexture() != null) {
                    region.getTexture().dispose();
                }
            }
        }
        if (brainTextures != null) {
            for (TextureRegion region : brainTextures) {
                if (region != null && region.getTexture() != null) {
                    region.getTexture().dispose();
                }
            }
        }
        if (eyebatTextures != null) {
            for (TextureRegion region : eyebatTextures) {
                if (region != null && region.getTexture() != null) {
                    region.getTexture().dispose();
                }
            }
        }
        if (xpDropTexture != null) {
            xpDropTexture.dispose();
        }
    }
}
