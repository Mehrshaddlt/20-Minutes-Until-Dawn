package untildawn.com.Model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
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
    private boolean bossSpawned = false;
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
        treeTextures = new TextureRegion[3];
        for (int i = 0; i < 3; i++) {
            Texture treeTexture = new Texture(Gdx.files.internal("assets/Enemies/Tree/T_TreeMonster_" + i + ".png"));
            treeTextures[i] = new TextureRegion(treeTexture);
        }
        brainTextures = new TextureRegion[7];
        for (int i = 0; i < 7; i++) {
            Texture brainTexture = new Texture(Gdx.files.internal("assets/Enemies/Brain/BrainMonster_" + i + ".png"));
            brainTextures[i] = new TextureRegion(brainTexture);
        }
        eyebatTextures = new TextureRegion[5];
        for (int i = 0; i < 5; i++) {
            Texture eyebatTexture = new Texture(Gdx.files.internal("assets/Enemies/Eyebat/T_EyeBat_" + i + ".png"));
            eyebatTextures[i] = new TextureRegion(eyebatTexture);
        }
    }

    public void spawnInitialTrees(int count) {
        enemies.clear();
        float minDistanceBetweenTrees = 150f;
        float edgeBuffer = 50f;
        float playerSafeRadius = 200f;
        float centerX = MAP_WIDTH / 2;
        float centerY = MAP_HEIGHT / 2;
        Random random = new Random();
        int gridSize = 200;
        int gridCols = (int)(MAP_WIDTH / gridSize);
        int gridRows = (int)(MAP_HEIGHT / gridSize);
        for (int i = 0; i < count; i++) {
            boolean validPositionFound = false;
            int attempts = 0;
            float x = 0, y = 0;
            while (!validPositionFound && attempts < 15) {
                int gridCol = random.nextInt(Math.max(1, gridCols));
                int gridRow = random.nextInt(Math.max(1, gridRows));
                x = (gridCol * gridSize) + random.nextFloat() * gridSize;
                y = (gridRow * gridSize) + random.nextFloat() * gridSize;
                if (x < edgeBuffer || x > MAP_WIDTH - edgeBuffer ||
                    y < edgeBuffer || y > MAP_HEIGHT - edgeBuffer) {
                    attempts++;
                    continue;
                }
                float dx = x - centerX;
                float dy = y - centerY;
                if (dx * dx + dy * dy < playerSafeRadius * playerSafeRadius) {
                    attempts++;
                    continue;
                }
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
        if (gameTime >= totalGameTime/2 && !bossSpawned) {
            spawnElderBoss(player);
            bossSpawned = true;
        }
        int spawnCount = (int)(gameTime / 30);
        if (spawnCount > 0 && lastBrainSpawnTime + 3 <= gameTime) {
            lastBrainSpawnTime = gameTime;
            for (int i = 0; i < spawnCount; i++) {
                spawnBrainMonster(player);
            }
        }
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            if (!enemy.isActive() && !enemy.hasAwardedXP()) {
                if (enemy instanceof BrainMonster) {
                    SoundManager.getInstance().play("brainmonsterDeath");
                }
                else if (enemy instanceof EyebatMonster) {
                    SoundManager.getInstance().play("bateyeDeath");
                }
                spawnXPDrop(enemy.getPosition().x, enemy.getPosition().y);
                enemy.setHasAwardedXP(true);
            }
        }
        if (gameTime >= totalGameTime / 4 && lastEyebatSpawnTime + 10 <= gameTime) {
            lastEyebatSpawnTime = gameTime;
            int eyebatSpawnCount = Math.max(0, (int)((4 * gameTime - totalGameTime + 30) / 30));
            eyebatSpawnCount = Math.min(eyebatSpawnCount, 2);
            for (int i = 0; i < eyebatSpawnCount; i++) {
                spawnEyebatMonster(player);
            }
        }
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
        Circle playerCircle = new Circle(player.getPosition(), 10);
        for (int i = xpDrops.size - 1; i >= 0; i--) {
            XPDrop drop = xpDrops.get(i);
            if (drop.isActive() && playerCircle.overlaps(drop.getCollisionCircle())) {
                player.gainXP(drop.getXpValue());
                drop.collect();
                xpDrops.removeIndex(i);
            }
        }
    }
    private void spawnBrainMonster(Player player) {
        float spawnX, spawnY;
        float buffer = 20f;
        int direction = random.nextInt(4);
        switch(direction) {
            case 0:
                spawnX = random.nextFloat() * MAP_WIDTH;
                spawnY = MAP_HEIGHT - buffer;
                break;
            case 1:
                spawnX = MAP_WIDTH - buffer;
                spawnY = random.nextFloat() * MAP_HEIGHT;
                break;
            case 2:
                spawnX = random.nextFloat() * MAP_WIDTH;
                spawnY = buffer;
                break;
            default:
                spawnX = buffer;
                spawnY = random.nextFloat() * MAP_HEIGHT;
                break;
        }
        BrainMonster brain = new BrainMonster(spawnX, spawnY, brainTextures);
        enemies.add(brain);
    }
    public void spawnElderBoss(Player player) {
        float spawnX = player.getPosition().x + 300;
        float spawnY = player.getPosition().y;

        ElderMonster boss = new ElderMonster(spawnX, spawnY, player);
        enemies.add(boss);
    }
    private void spawnEyebatMonster(Player player) {
        float spawnX, spawnY;
        float buffer = 20f;
        int direction = random.nextInt(4);
        switch(direction) {
            case 0:
                spawnX = random.nextFloat() * MAP_WIDTH;
                spawnY = MAP_HEIGHT - buffer;
                break;
            case 1:
                spawnX = MAP_WIDTH - buffer;
                spawnY = random.nextFloat() * MAP_HEIGHT;
                break;
            case 2:
                spawnX = random.nextFloat() * MAP_WIDTH;
                spawnY = buffer;
                break;
            default:
                spawnX = buffer;
                spawnY = random.nextFloat() * MAP_HEIGHT;
                break;
        }
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
        Array<Enemy> trees = new Array<>();
        for (Enemy enemy : enemies) {
            if (enemy instanceof TreeMonster) {
                trees.add(enemy);
            }
        }
        enemies.clear();
        enemies.addAll(trees);
    }
    public Array<Enemy> getEnemies() {
        return enemies;
    }
    public void clearAllEnemies() {
        enemies.clear();
    }
    public void dispose() {
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
