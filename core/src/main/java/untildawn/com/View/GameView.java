package untildawn.com.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import untildawn.com.Controller.EndScreenController;
import untildawn.com.Controller.GameController;
import untildawn.com.Controller.PauseMenuController;
import untildawn.com.Controller.SettingsMenuController;
import untildawn.com.Main;
import untildawn.com.Model.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;


public class GameView implements Screen {
    private final Main game;
    private final GameController controller;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private Texture shadowTexture;
    private Vector3 cursorPosition;
    private Vector2 worldCursorPosition;
    private WeaponsManager weaponsManager;
    private String selectedWeapon;
    private Texture cursorTexture;
    private BitmapFont font;
    private OrthographicCamera uiCamera;
    private BitmapFont pixelFont;
    private EnemiesManager enemiesManager;
    private float gameTimeRemaining;
    private boolean gameTimerActive = true;
    private ShapeRenderer shapeRenderer;
    private Abilities abilities;
    private boolean leftButtonWasPressed = false;
    private PauseMenuController pauseMenuController;
    private PauseMenuView pauseMenuView;
    private Texture fogOfWarTexture;
    private SpriteBatch fogBatch;
    private float damageFeedbackTimer = 0;
    private boolean isPlayerHit = false;
    private final float DAMAGE_FEEDBACK_DURATION = 0.3f; // How long the red flash lasts
    private Texture redFlashTexture;
    private boolean lowHealthSoundPlayed = false;
    //Camera
    private final float cameraZoom = 0.5f;
    private MusicManager musicManager;
    // Map elements
    private Texture grassTexture;
    private int tileSize;

    // Animation timing
    private final float frameDuration = 0.15f;
    private BitmapFont killCountFont;
    // Map dimensions
    private final int MAP_WIDTH = 60;
    private final int MAP_HEIGHT = 40;
    // Gray scale
    private ShaderProgram grayscaleShader;
    private boolean isGrayscale = false;
    public GameView(Main game, User user, int durationMinutes) {
        this.game = game;
        this.weaponsManager = new WeaponsManager();
        this.batch = new SpriteBatch();
        this.gameTimeRemaining = durationMinutes * 60;
        this.shapeRenderer = new ShapeRenderer();
        // Initialize UI cameras, fonts, etc.
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.update();
        SettingsMenuController settingsMenuController = new SettingsMenuController(game, user);
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        loadTTFFont();
        musicManager = MusicManager.getInstance();
        musicManager.playMusic("Gameplay");
        // Initialize camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.zoom = cameraZoom;
        camera.update();
        killCountFont = new BitmapFont();
        killCountFont.setColor(Color.WHITE);
        killCountFont.getData().setScale(1.5f);
        // Load textures
        ShaderProgram.pedantic = false;
        grayscaleShader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"), // Use LibGDX's default vertex shader
            Gdx.files.internal("shaders/grayscale.frag")
        );
        if (!grayscaleShader.isCompiled()) {
            System.err.println("Grayscale shader compile error: " + grayscaleShader.getLog());
        }
        grassTexture = new Texture(Gdx.files.internal("T_TileGrass.png"));
        cursorTexture = new Texture(Gdx.files.internal("T_Cursor.png"));
        try {
            shadowTexture = new Texture(Gdx.files.internal("T_CharacterShadow.png"));
        } catch (Exception e) {
            // Try alternative extension if the first fails
            try {
                shadowTexture = new Texture(Gdx.files.internal("T_CharacterShadow.pmg"));
            } catch (Exception e2) {
                Gdx.app.error("GameView", "Failed to load shadow texture", e2);
            }
        }

        tileSize = grassTexture.getWidth();

        // FIRST: Initialize EnemiesManager before creating controller
        enemiesManager = new EnemiesManager(MAP_WIDTH * tileSize, MAP_HEIGHT * tileSize, weaponsManager);
        enemiesManager.spawnInitialTrees(20);

        // THEN: Create controller with the initialized enemiesManager
        this.controller = new GameController(user, weaponsManager, enemiesManager, (int)gameTimeRemaining, settingsMenuController);

        // Rest of initialization
        Gdx.input.setCursorCatched(true);
        cursorPosition = new Vector3();
        worldCursorPosition = new Vector2();
        pauseMenuController = new PauseMenuController(controller, game, this);
        pauseMenuView = new PauseMenuView(pauseMenuController);
        selectedWeapon = user.getSelectedWeapon();
        abilities = new Abilities(controller.getPlayer());
        abilities.setWeaponsManager(weaponsManager);
        // Set player to start in the center of the map
        float startX = MAP_WIDTH * tileSize / 2;
        float startY = MAP_HEIGHT * tileSize / 2;
        controller.getPlayer().setPosition(startX, startY);
        initFogOfWar();
        initDamageEffect();
    }

    @Override
    public void render(float delta) {
        // Check if player leveled up

        Player player = controller.getPlayer();
        if (player.getLevel() > player.lastLevel) {
            player.lastLevel = player.getLevel();
            abilities.showAbilitySelection();
        }
        if (player.isHit()) {
            setPlayerHit(); // Trigger the damage visual effect
        }

        // Continue with existing damage timer logic
        if (damageFeedbackTimer > 0) {
            damageFeedbackTimer -= delta;
            if (damageFeedbackTimer <= 0) {
                isPlayerHit = false;
            }
        }
        // Handle pause input before any updates
        handlePauseInput();

        // Check victory/defeat conditions ONLY if not paused
        if (!pauseMenuController.isPaused()) {
            // Victory condition - timer reached 0
            if (gameTimerActive && gameTimeRemaining > 0) {
                gameTimeRemaining -= delta;
                if (gameTimeRemaining <= 0) {
                    gameTimeRemaining = 0;
                    // Player won! Show victory end screen
                    SoundManager.getInstance().play("win", 0.8f);
                    Texture backgroundTexture = captureScreen();

                    // Player won! Show victory screen with background
                    EndScreenController endScreenController = new EndScreenController(
                        game,
                        controller,
                        true, // game won
                        (int)gameTimeRemaining,
                        controller.getPlayer().getUser()
                    );
                    game.setScreen(new EndScreenView(game, endScreenController, (int)gameTimeRemaining, backgroundTexture));
                    dispose();
                    return;
                }
            }
            if (player.getCurrentHealth() == 1) {
                if (!lowHealthSoundPlayed) {
                    SoundManager.getInstance().play("lowhealth", 0.7f);
                    lowHealthSoundPlayed = true;
                }
            } else {
                // Reset the flag when health is not 1
                lowHealthSoundPlayed = false;
            }
            // Defeat condition - health reached 0
            if (player.getCurrentHealth() <= 0) {
                // Player lost! Show defeat end screen
                Texture backgroundTexture = captureScreen();
                SoundManager.getInstance().play("lost", 0.8f);
                // Player lost! Show defeat screen with background
                EndScreenController endScreenController = new EndScreenController(
                    game,
                    controller,
                    false, // game lost
                    (int)gameTimeRemaining,
                    controller.getPlayer().getUser()
                );
                game.setScreen(new EndScreenView(game, endScreenController, (int)gameTimeRemaining, backgroundTexture));
                dispose();
                return;
            }

            // Only update if NOT paused
            controller.update(delta);
        }

        // Always update camera and clear screen
        enforceMapBoundaries();
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        updateCamera();

        // Only process menu navigation if NOT paused
        if (!pauseMenuController.isPaused()) {
            // Handle menu navigation
            if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
                toggleGrayscale();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                game.setScreen(new PreMenuView(game));
                dispose();
                return;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
                // Skip 1 minute (60 seconds)
                gameTimeRemaining = Math.max(0, gameTimeRemaining - 60);
                System.out.println("Time skipped! Remaining: " + (int)gameTimeRemaining + " seconds");
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
                player.gainXP(player.getXpToNextLevel() - player.getCurrentXP() + 1);
                System.out.println("Forced level up! New level: " + player.getLevel());
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
                player.restoreFullHealth();
            }
        }

        // Track cursor position (always needed for rendering)
        cursorPosition.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(cursorPosition);
        worldCursorPosition.set(cursorPosition.x, cursorPosition.y);

        // Different logic based on ability selection
        if (abilities.isSelectionActive()) {
            batch.begin();
            // Draw frozen game world
            drawFixedMap();
            weaponsManager.renderBullets(batch);
            enemiesManager.render(batch);
            drawPlayer();
            drawWeapon();
            drawLevelUpAnimation();
            batch.end();

            // Draw fog of war between batch operations
            drawVisionCircle();

            batch.begin();
            // Draw ability selection UI (this now handles its own projection)
            abilities.render(batch);

            // Draw cursor so player can see where they're clicking
            Matrix4 gameProjection = batch.getProjectionMatrix();
            batch.setProjectionMatrix(uiCamera.combined);
            batch.draw(cursorTexture, Gdx.input.getX() - 15, Gdx.graphics.getHeight() - Gdx.input.getY() - 15, 30, 30);
            batch.setProjectionMatrix(gameProjection);
            if (isGrayscale && grayscaleShader != null && grayscaleShader.isCompiled()) {
                batch.setShader(grayscaleShader);
            } else {
                batch.setShader(null);
            }
            batch.end();
            if (isPlayerHit) {
                drawDamageFlash();
            }
            // Handle ability selection with screen coordinates
            if (Gdx.input.justTouched()) {
                // Get screen coordinates
                int touchX = Gdx.input.getX();
                int touchY = Gdx.input.getY();

                // Calculate card positions in screen space - MATCH EXACTLY with Abilities.render()
                float screenWidth = Gdx.graphics.getWidth();
                float screenHeight = Gdx.graphics.getHeight();

                // Use the SAME values as in Abilities.render()
                float cardWidth = 150;  // Match the new width
                float cardHeight = 150;
                float spacing = 60;   // Spacing between cards
                float totalWidth = (cardWidth * 3) + (spacing * 2);

                // Position cards below character - 25% from bottom as in Abilities.render()
                float startX = (screenWidth - totalWidth) / 2;
                float startY = screenHeight * 0.25f;

                // Convert touchY to match our coordinate system
                touchY = Gdx.graphics.getHeight() - touchY;

                // Check which card was clicked
                for (int i = 0; i < 3; i++) {
                    float x = startX + (i * (cardWidth + spacing));
                    if (touchX >= x && touchX <= x + cardWidth &&
                        touchY >= startY && touchY <= startY + cardHeight) {
                        abilities.selectAbility(i);
                        break;
                    }
                }
            }
        }
        else if (!pauseMenuController.isPaused()) {
            Vector2 aimTarget = controller.getAutoAimTarget();
            Vector2 targetToUse = aimTarget != null ? aimTarget : worldCursorPosition;

            // Update mouse cursor position to match the target
            if (aimTarget != null) {
                // Convert world position to screen coordinates
                Vector3 screenPos = camera.project(new Vector3(aimTarget.x, aimTarget.y, 0));
                Gdx.input.setCursorPosition((int)screenPos.x, Gdx.graphics.getHeight() - (int)screenPos.y);
            }

            // Then use targetToUse for weapon angle and rendering
            float angle = calculateAngleBetween(player.getPosition(), targetToUse);
            controller.updateTargetInfo(targetToUse, angle);

            // Handle weapon firing
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                controller.handleShot(
                    player.getPosition(),
                    targetToUse,
                    selectedWeapon,
                    angle
                );

                if (selectedWeapon.equals("SMG")) {
                    controller.startContinuousFire();
                }
            }

            if (leftButtonWasPressed && !Gdx.input.isButtonPressed(Input.Buttons.LEFT) && selectedWeapon.equals("SMG")) {
                controller.stopContinuousFire();
            }
            leftButtonWasPressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);

            // Update enemies and weapons
            enemiesManager.update(delta, controller.getPlayer(), gameTimeRemaining);
            weaponsManager.checkEnemyCollisions(enemiesManager.getEnemies(), player);
            weaponsManager.updateEffects(delta);

            // Update active ability effects
            abilities.update(delta);

            batch.begin();
            drawFixedMap();
            weaponsManager.renderBullets(batch);
            enemiesManager.render(batch);
            drawPlayer();
            drawWeapon();
            weaponsManager.renderMuzzleFlash(batch);
            weaponsManager.renderEffects(batch);
            weaponsManager.renderReloadUI(batch, player.getPosition());
            batch.end();

            // Draw fog of war between batch operations
            drawVisionCircle();

            batch.begin();
            drawCursor();
            drawUI(delta);
            renderLevelBar(batch);
            if (isGrayscale && grayscaleShader != null && grayscaleShader.isCompiled()) {
                batch.setShader(grayscaleShader);
            } else {
                batch.setShader(null);
            }
            batch.end();
            if (isPlayerHit) {
                drawDamageFlash();
            }
        } else {
            // We're paused but not in ability selection - render the frozen game state
            batch.begin();
            drawFixedMap();
            weaponsManager.renderBullets(batch);
            drawLevelUpAnimation();
            enemiesManager.render(batch);
            drawPlayer();
            drawWeapon();
            weaponsManager.renderEffects(batch);
            batch.end();

            // Draw fog of war between batch operations
            drawVisionCircle();

            batch.begin();
            drawUI(delta);
            renderLevelBar(batch);
            if (isGrayscale && grayscaleShader != null && grayscaleShader.isCompiled()) {
                batch.setShader(grayscaleShader);
            } else {
                batch.setShader(null);
            }
            batch.end();
            if (isPlayerHit) {
                drawDamageFlash();
            }
            batch.setShader(null);
        }

        // Always render pause menu on top if paused
        if (pauseMenuController.isPaused()) {
            pauseMenuView.render();
        }
    }
    public void toggleGrayscale() {
        isGrayscale = !isGrayscale;
    }
    private void initFogOfWar() {
        fogBatch = new SpriteBatch();
        int texSize = 512;
        Pixmap pixmap = new Pixmap(texSize, texSize, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);

        float radius = texSize / 2f;
        for (int x = 0; x < texSize; x++) {
            for (int y = 0; y < texSize; y++) {
                float distFromCenter = Vector2.dst(x, y, radius, radius) / radius;

                // Create a much heavier fog effect
                float alpha;
                if (distFromCenter < 0.15f) {
                    // Much smaller clear center (15% of radius)
                    alpha = 0f;
                } else if (distFromCenter < 0.4f) {
                    // Very steep gradient for dramatic transition
                    float t = (distFromCenter - 0.15f) / 0.25f;
                    alpha = t * t * 0.98f; // Sharper quadratic transition
                } else {
                    // Nearly complete darkness (98% black)
                    alpha = 0.98f;
                }

                // Draw with a slight blue tint to make it more noticeable
                pixmap.drawPixel(x, y, Color.rgba8888(0.02f, 0.02f, 0.05f, alpha));
            }
        }

        fogOfWarTexture = new Texture(pixmap);
        pixmap.dispose();
    }
    private void drawVisionCircle() {
        Player player = controller.getPlayer();
        float playerX = player.getPosition().x;
        float playerY = player.getPosition().y;

        // Set up proper blending for the fog
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Use a dedicated batch for fog rendering
        fogBatch.setProjectionMatrix(camera.combined);
        fogBatch.begin();

        // Size of fog - make it large enough to cover the screen
        float fogSize = camera.viewportWidth * camera.zoom * 2;

        // Draw fog centered on player
        fogBatch.draw(
            fogOfWarTexture,
            playerX - fogSize/2,
            playerY - fogSize/2,
            fogSize, fogSize
        );

        fogBatch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }
    private void drawLevelUpAnimation() {
        Player player = controller.getPlayer();
        if (player.getLevelUpAnimation() != null && player.getLevelUpAnimation().isActive()) {
            player.getLevelUpAnimation().render(batch);
        }
    }
    private void handlePauseInput() {
        // Check for pause key press (ESC)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pauseMenuController.togglePause();

            // Handle cursor visibility when toggling pause
            if (pauseMenuController.isPaused()) {
                // When pausing: Show system cursor and set menu stage as input processor
                Gdx.input.setCursorCatched(false);
                Gdx.input.setInputProcessor(pauseMenuView.getStage());
            } else {
                // When unpausing: Hide system cursor and restore game input handling
                Gdx.input.setCursorCatched(true);
                Gdx.input.setInputProcessor(null); // Let the game handle input directly
            }
        }

        // For mouse clicks while paused, ensure they're handled by the stage
        if (pauseMenuController.isPaused() && Gdx.input.justTouched()) {
            pauseMenuView.handleClick(Gdx.input.getX(), Gdx.input.getY());
        }
    }
    private void drawCursor() {
        if (cursorTexture != null) {
            float scale = 0.6f;
            float cursorWidth = cursorTexture.getWidth() * scale;
            float cursorHeight = cursorTexture.getHeight() * scale;
            batch.draw(cursorTexture,
                worldCursorPosition.x - cursorWidth/2,
                worldCursorPosition.y - cursorHeight/2,
                cursorWidth,
                cursorHeight);
        }
    }
    private void drawUI(float delta) {
        // Save the current projection matrix
        Matrix4 gameProjection = batch.getProjectionMatrix();
        // Switch to UI camera for HUD elements
        batch.setProjectionMatrix(uiCamera.combined);

        // Draw timer in top right corner with "Survive!" text above it
        int minutes = (int)(gameTimeRemaining / 60);
        int seconds = (int)(gameTimeRemaining % 60);
        String timeText = String.format("%02d:%02d", minutes, seconds);

        float timerX = Gdx.graphics.getWidth() - 150; // Moved more to the left
        float timerY = Gdx.graphics.getHeight() - 40;

        // Use the custom TTF font with larger scale for timer
        if (pixelFont != null) {
            float originalScale = pixelFont.getData().scaleX;

            // Draw "Survive!" text in gray ABOVE the timer
            pixelFont.getData().setScale(1.0f); // Smaller text
            pixelFont.setColor(0.7f, 0.7f, 0.7f, 1.0f); // Gray color
            pixelFont.draw(batch, "Survive!", timerX - 10, timerY + 25);

            // Draw main timer with larger scale
            pixelFont.getData().setScale(2f);
            pixelFont.setColor(1.0f, 1.0f, 1.0f, 1.0f); // Reset to white
            pixelFont.draw(batch, timeText, timerX, timerY);

            // Reset font properties
            pixelFont.getData().setScale(originalScale);
        }

        // Sizes for UI elements
        float iconWidth = 50;
        float iconHeight = 80; // Increased height for the bullet
        float heartSize = 64;
        float heartSpacing = 4;// Close spacing for hearts

        // Draw ammo icon in top left
        Texture ammoIcon = CharacterStats.getAmmoIcon();
        if (ammoIcon != null) {
            batch.draw(ammoIcon, 20, Gdx.graphics.getHeight() - iconHeight - 20, iconWidth, iconHeight);
        }

        // Get ammo count with 3-digit format
        int currentAmmo = weaponsManager.getCurrentAmmo(selectedWeapon);
        int maxAmmo = weaponsManager.getMaxAmmo(selectedWeapon);
        String ammoText = String.format("%03d/%03d", currentAmmo, maxAmmo);

        // Calculate text position to vertically center with the ammo icon
        float textY = Gdx.graphics.getHeight() - iconHeight - 20 + (iconHeight / 2) + 10; // +10 for baseline adjustment

        // Use the custom TTF font we loaded with smaller scale
        if (pixelFont != null) {
            float originalScale = pixelFont.getData().scaleX;
            pixelFont.getData().setScale(1.2f);
            pixelFont.draw(batch, ammoText, 95, textY);
            pixelFont.getData().setScale(originalScale);
        } else {
            // Try to get a font from the skin if TTF font failed
            BitmapFont uiFont = null;
            try {
                uiFont = AssetManager.getInstance().getSkin().getFont("default-font");
                float originalScale = uiFont.getData().scaleX;
                uiFont.getData().setScale(1.2f);
                uiFont.draw(batch, ammoText, 95, textY);
                uiFont.getData().setScale(originalScale);
            } catch (Exception e) {
                // Fall back to default font if everything else fails
                font.getData().setScale(1.2f);
                font.draw(batch, ammoText, 95, textY);
                font.getData().setScale(1.0f);
            }
        }

        // Draw heart animation - one heart per health point
        Player player = controller.getPlayer();
        int currentHealth = player.getCurrentHealth();
        int maxHealth = player.getMaxHealth();
        // Hearts row calculations
        // Custom heartbeat animation logic
        float heartbeatTime = player.getStateTime() % 2.0f; // 2 second cycle
        TextureRegion heartFrame;

        // Two quick beats followed by a pause
        if (heartbeatTime < 0.15f) {
            // First beat
            heartFrame = new TextureRegion(CharacterStats.getHeartAnimation().getKeyFrames()[1]);
        } else if (heartbeatTime < 0.3f) {
            // Return to normal
            heartFrame = new TextureRegion(CharacterStats.getHeartAnimation().getKeyFrames()[0]);
        } else if (heartbeatTime < 0.45f) {
            // Second beat
            heartFrame = new TextureRegion(CharacterStats.getHeartAnimation().getKeyFrames()[1]);
        } else {
            // Long rest period
            heartFrame = new TextureRegion(CharacterStats.getHeartAnimation().getKeyFrames()[0]);
        }
        String killText = "Kills: " + player.getKillCount();
        GlyphLayout layout = new GlyphLayout(pixelFont != null ? pixelFont : font, killText);
        float killX = (Gdx.graphics.getWidth() - layout.width) / 2;
        float killY = Gdx.graphics.getHeight() - 20;

        if (pixelFont != null) {
            float originalScale = pixelFont.getData().scaleX;
            pixelFont.getData().setScale(1.5f);
            pixelFont.draw(batch, killText, killX, killY);
            pixelFont.getData().setScale(originalScale);
        } else {
            font.draw(batch, killText, killX, killY);
        }
        // Hearts row calculations
        int heartsPerRow = (int)((Gdx.graphics.getWidth() - 40) / (heartSize + heartSpacing));
        if (heartsPerRow <= 0) heartsPerRow = 1;

        for (int i = 0; i < maxHealth; i++) {
            int row = i / heartsPerRow;
            int col = i % heartsPerRow;

            float heartX = 20 + col * (heartSize + heartSpacing);
            float heartY = Gdx.graphics.getHeight() - iconHeight - 40 - heartSize - (row * (heartSize + heartSpacing));

            if (i < currentHealth) {
                if (heartFrame != null) {
                    batch.draw(heartFrame, heartX, heartY, heartSize, heartSize);
                }
            } else {
                Texture emptyHeart = CharacterStats.getEmptyHeart();
                if (emptyHeart != null) {
                    batch.draw(emptyHeart, heartX, heartY, heartSize, heartSize);
                }
            }
        }

        // Restore game projection matrix
        batch.setProjectionMatrix(gameProjection);
    }
    private void drawPlayer() {
        Player player = controller.getPlayer();
        String character = player.getCharacterName();
        TextureRegion currentFrame = null;

        // Get the appropriate animation frame based on player state
        switch (player.getState()) {
            case IDLE:
                Array<TextureRegion> idleFrames = controller.getIdleManager().getIdleFrames(character);
                int idleFrameIndex = (int)(player.getStateTime() / frameDuration) % idleFrames.size;
                currentFrame = idleFrames.get(idleFrameIndex);
                break;
            case WALKING:
                Array<TextureRegion> walkFrames = controller.getMovementManager().getWalkFrames(character);
                int walkFrameIndex = (int)(player.getStateTime() / frameDuration) % walkFrames.size;
                currentFrame = walkFrames.get(walkFrameIndex);
                break;
            case RUNNING:
                Array<TextureRegion> runFrames = controller.getMovementManager().getRunFrames(character);
                int runFrameIndex = (int)(player.getStateTime() / frameDuration) % runFrames.size;
                currentFrame = runFrames.get(runFrameIndex);
                break;
        }

        if (currentFrame != null) {
            float drawX = player.getPosition().x - currentFrame.getRegionWidth() / 2;
            float drawY = player.getPosition().y - currentFrame.getRegionHeight() / 2;

            // Draw shadow underneath player
            if (shadowTexture != null) {
                float shadowX = player.getPosition().x - shadowTexture.getWidth() / 2;
                // Position shadow lower and make sure it's visible
                float shadowY = player.getPosition().y - shadowTexture.getHeight() / 2 - 15;
                batch.draw(shadowTexture, shadowX, shadowY);
            }

            // Determine if sprite should be flipped based on direction
            boolean flipX = player.getDirection() == Player.Direction.LEFT;

            if (isPlayerHit && damageFeedbackTimer > 0) {
                batch.setColor(1, 0.1f, 0.1f, 1);  // More intense red
            } else {
                batch.setColor(1, 1, 1, 1); // Normal color
            }
            // Draw character with proper orientation
            if (flipX) {
                // Draw flipped sprite
                batch.draw(currentFrame.getTexture(),
                    drawX + currentFrame.getRegionWidth(), drawY,
                    -currentFrame.getRegionWidth(), currentFrame.getRegionHeight(),
                    currentFrame.getRegionX(), currentFrame.getRegionY(),
                    currentFrame.getRegionWidth(), currentFrame.getRegionHeight(),
                    false, false);
            }
            else {
                // Draw normal sprite
                batch.draw(currentFrame, drawX, drawY);
            }
            batch.setColor(1, 1, 1, 1);
        }
    }
    private void drawWeapon() {
        Player player = controller.getPlayer();
        TextureRegion weaponTexture = weaponsManager.getWeaponTexture(selectedWeapon);
        if (weaponsManager.isReloading()) {
            return;
        }
        // Calculate angle from player to cursor
        float angle = calculateAngleBetween(player.getPosition(), worldCursorPosition);

        // Calculate weapon position (offset from player)
        float offsetDistance = 10; // Closer to player
        float weaponX = player.getPosition().x + offsetDistance * (float)Math.cos(Math.toRadians(angle));
        float weaponY = player.getPosition().y + offsetDistance * (float)Math.sin(Math.toRadians(angle));

        // Adjust weapon origin point for rotation
        float originX = weaponTexture.getRegionWidth() * 0.3f;
        float originY = weaponTexture.getRegionHeight() * 0.5f;

        // Flip weapon when pointing left
        boolean flipY = (angle > 90 || angle < -90);

        // Draw weapon with increased scale (1.8 = 80% larger)
        float scale = 1.5f;

        batch.draw(
            weaponTexture,
            weaponX - originX,
            weaponY - originY,
            originX,
            originY,
            weaponTexture.getRegionWidth(),
            weaponTexture.getRegionHeight(),
            scale,
            flipY ? -scale : scale,
            angle
        );
    }
    private float calculateAngleBetween(Vector2 from, Vector2 to) {
        return (float) Math.toDegrees(Math.atan2(to.y - from.y, to.x - from.x));
    }
    private void enforceMapBoundaries() {
        float playerX = controller.getPlayer().getPosition().x;
        float playerY = controller.getPlayer().getPosition().y;

        // Calculate map boundaries
        float minX = 0;
        float minY = 0;
        float maxX = MAP_WIDTH * tileSize;
        float maxY = MAP_HEIGHT * tileSize;

        // Clamp player position
        float clampedX = Math.max(minX, Math.min(maxX, playerX));
        float clampedY = Math.max(minY, Math.min(maxY, playerY));

        controller.getPlayer().setPosition(clampedX, clampedY);
    }

    private void updateCamera() {
        // Center camera on player
        camera.position.set(
            controller.getPlayer().getPosition().x,
            controller.getPlayer().getPosition().y,
            0
        );
        camera.update();
        batch.setProjectionMatrix(camera.combined);
    }
    private void renderLevelBar(SpriteBatch batch) {
        // Set up dimensions for the bar
        float barHeight = 35;
        float barY = 40;
        float screenWidth = Gdx.graphics.getWidth();
        float margin = 20f; // 20 pixel margin from screen edges

        float barWidth = screenWidth - (margin * 2); // Adjusted width with margins
        float barX = margin; // Start from left margin

        // End the sprite batch if it's active
        boolean batchWasActive = false;
        if (batch.isDrawing()) {
            batch.end();
            batchWasActive = true;
        }

        // Set the UI projection matrix for the shape renderer - THIS FIXES THE BUG
        shapeRenderer.setProjectionMatrix(uiCamera.combined);

        // Draw the background (dark green rectangle)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.3f, 0.1f, 1); // Dark green
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        // Draw the progress (light green rectangle)
        Player player = controller.getPlayer();
        float progress = player.getLevelProgress();
        shapeRenderer.setColor(0.3f, 0.8f, 0.3f, 1); // Light green
        shapeRenderer.rect(barX, barY, barWidth * progress, barHeight);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Start batch for text rendering
        batch.begin();

        // Draw level text
        String levelText = "Level " + player.getLevel();
        GlyphLayout layout = new GlyphLayout(pixelFont, levelText);
        float textX = barX + (barWidth - layout.width) / 2; // Center text in the bar
        float textY = barY + (barHeight + layout.height) / 2;

        pixelFont.draw(batch, levelText, textX, textY);

        // If batch wasn't active before, end it
        if (!batchWasActive) {
            batch.end();
        }
    }
    private void drawFixedMap() {
        // Calculate the visible area based on camera position
        float leftEdge = camera.position.x - camera.viewportWidth/2 - tileSize;
        float bottomEdge = camera.position.y - camera.viewportHeight/2 - tileSize;
        float rightEdge = camera.position.x + camera.viewportWidth/2 + tileSize;
        float topEdge = camera.position.y + camera.viewportHeight/2 + tileSize;

        // Determine which tiles are visible
        int startX = Math.max(0, (int)(leftEdge / tileSize));
        int startY = Math.max(0, (int)(bottomEdge / tileSize));
        int endX = Math.min(MAP_WIDTH, (int)Math.ceil(rightEdge / tileSize));
        int endY = Math.min(MAP_HEIGHT, (int)Math.ceil(topEdge / tileSize));

        // Draw only the visible tiles
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                float tileX = x * tileSize;
                float tileY = y * tileSize;
                batch.draw(grassTexture, tileX, tileY);
            }
        }
    }
    private void initDamageEffect() {
        // Create a 1x1 red pixel texture for full screen overlay
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 0, 0, 0.5f); // Semi-transparent red
        pixmap.fill();
        redFlashTexture = new Texture(pixmap);
        pixmap.dispose();
    }
    private void loadTTFFont() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("assets/ChevyRay - Express.ttf"));
            FreeTypeFontParameter parameter = new FreeTypeFontParameter();
            parameter.size = 24;
            parameter.color = Color.WHITE;
            parameter.borderWidth = 1;
            parameter.borderColor = Color.BLACK;
            pixelFont = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            System.err.println("Failed to load TTF font: " + e.getMessage());
            pixelFont = new BitmapFont();
        }
    }
    public void setPlayerHit() {
        isPlayerHit = true;
        damageFeedbackTimer = 0.15f;
        SoundManager.getInstance().play("impact", 0.5f);
    }
    // Add this method to render the red flash
    private void drawDamageFlash() {
        try {
            // Switch to UI projection for full screen effect
            shapeRenderer.setProjectionMatrix(uiCamera.combined);

            // Calculate flash intensity that fades quickly
            float alpha = damageFeedbackTimer / DAMAGE_FEEDBACK_DURATION * 0.5f;

            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1, 0, 0, alpha);

            // Draw rectangle covering entire screen in UI coordinates
            shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);

            // Reset to game camera projection
            shapeRenderer.setProjectionMatrix(camera.combined);
        } catch (Exception e) {
            System.err.println("Error drawing damage flash: " + e.getMessage());
        }
    }
    private Texture captureScreen() {
        // Store original camera position
        Vector3 originalCameraPosition = new Vector3(camera.position);
        float originalZoom = camera.zoom;

        // Pause the game
        boolean wasPaused = pauseMenuController.isPaused();
        pauseMenuController.setPaused(true);

        // Clear mobile enemies but keep trees
        enemiesManager.clearMobileEnemies();

        // Make sure camera is centered on player
        camera.position.set(
            controller.getPlayer().getPosition().x,
            controller.getPlayer().getPosition().y,
            0
        );
        camera.zoom = cameraZoom;
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Render one frame
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        drawFixedMap();
        enemiesManager.render(batch); // Renders trees
        drawPlayer();
        drawWeapon();
        batch.end();

        // Capture the screen
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        Pixmap originalPixmap = ScreenUtils.getFrameBufferPixmap(0, 0, width, height);

        // Create a new pixmap and flip the original vertically
        Pixmap flippedPixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                flippedPixmap.drawPixel(x, y, originalPixmap.getPixel(x, height - 1 - y));
            }
        }

        // Create texture from the flipped pixmap
        Texture backgroundTexture = new Texture(flippedPixmap);

        // Dispose of pixmaps
        originalPixmap.dispose();
        flippedPixmap.dispose();

        // Restore camera position
        camera.position.set(originalCameraPosition);
        camera.zoom = originalZoom;
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Restore pause state
        pauseMenuController.setPaused(wasPaused);

        return backgroundTexture;
    }
    public Texture getScreenshot() {
        return captureScreen();
    }
    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        camera.zoom = cameraZoom;  // Reapply zoom when resizing
        camera.update();

        // Update UI camera too
        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();
    }

    @Override
    public void show() {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        grassTexture.dispose();
        shadowTexture.dispose();
        weaponsManager.dispose();
        controller.getMovementManager().dispose();
        controller.getIdleManager().dispose();
        cursorTexture.dispose();
        font.dispose();
        musicManager.stopMusic();
        Gdx.input.setCursorCatched(false);
        if (enemiesManager != null) {
            enemiesManager.dispose();
        }
        if (pixelFont != null) {
            pixelFont.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (killCountFont != null) {
            killCountFont.dispose();
        }
        if (fogOfWarTexture != null) fogOfWarTexture.dispose();
        if (fogBatch != null) fogBatch.dispose();
        if (redFlashTexture != null) redFlashTexture.dispose();
    }
}
