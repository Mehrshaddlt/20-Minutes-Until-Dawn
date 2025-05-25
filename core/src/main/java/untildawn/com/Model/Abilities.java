package untildawn.com.Model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.Gdx;

import java.util.HashMap;
import java.util.Random;

public class Abilities {
    // All available abilities
    private Array<Ability> allAbilities;
    private BitmapFont pixelFont;
    // Currently active abilities with timers
    private Array<ActiveAbility> activeAbilities;
    private WeaponsManager weaponsManager;
    // UI textures
    private Texture frameTexture;
    private Texture backgroundTexture;

    // Random generator for ability selection
    private Random random;

    // Current selection of abilities to choose from
    private Array<Ability> currentSelection;

    // Reference to player to apply effects
    private Player player;

    // Selection state
    private boolean selectionActive;
    public void setWeaponsManager(WeaponsManager weaponsManager) {
        this.weaponsManager = weaponsManager;
        initializeAbilities();
    }
    public Abilities(Player player) {
        this.player = player;
        random = new Random();
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("assets/ChevyRay - Express.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 24;
            parameter.color = Color.WHITE;
            parameter.borderWidth = 2;
            parameter.borderColor = Color.BLACK;
            pixelFont = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            System.err.println("Failed to load TTF font: " + e.getMessage());
            pixelFont = new BitmapFont();
        }
        allAbilities = new Array<>(5);
        activeAbilities = new Array<>();
        currentSelection = new Array<>(3);

        // Load UI textures
        frameTexture = new Texture(Gdx.files.internal("assets/Abilities/PowerupFrame.png"));
        backgroundTexture = new Texture(Gdx.files.internal("assets/Abilities/PowerupIconBG.png"));
    }

    private void initializeAbilities() {
        // Create the 5 abilities
        Ability amocrease = new Ability("Amocrease", "Adds 5 extra ammo to all weapons",
            new Texture(Gdx.files.internal("assets/Abilities/Amocrease/Icon_Amocrease.png")),
            player -> {
                player.activateAmmoIncrease();
                // Call method directly on weaponsManager
                weaponsManager.addExtraAmmoToCurrentWeapon("Revolver", 5);
                weaponsManager.addExtraAmmoToCurrentWeapon("Shotgun", 5);
                weaponsManager.addExtraAmmoToCurrentWeapon("SMG", 5);

                weaponsManager.addExtraAmmoCapacityToWeapon("Revolver", 5);
                weaponsManager.addExtraAmmoCapacityToWeapon("Shotgun", 5);
                weaponsManager.addExtraAmmoCapacityToWeapon("SMG", 5);

                return null; // No timer needed for one-time effect
            });


        Ability vitality = new Ability("Vitality", "Adds 1 Heart",
            new Texture(Gdx.files.internal("assets/Abilities/Vitality/Icon_Vitality.png")),
            player -> {
                // Add one heart to player's maximum health capacity
                player.addMaxHearts(1);
                player.activateVitality();
                // Also heal to full health (optional but common in games)
                player.restoreFullHealth();
                return null; // No active effect
            });

        Ability damager = new Ability("Damager", "Increases damage by 25% for 10 seconds",
            new Texture(Gdx.files.internal("assets/Abilities/Damager/Icon_Damager.png")),
            player -> {
                // Return active effect with timer
                return new ActiveAbility("Damager", 10f,
                    remainingTime -> player.setDamageMultiplier(1.25f),
                    () -> player.setDamageMultiplier(1.0f));
            });

        Ability procrease = new Ability("Procrease", "Fires an additional bullet with every shot for 10 seconds",
            new Texture(Gdx.files.internal("assets/Abilities/Procrease/Icon_Procrease.png")),
            player -> {
                // Return active effect with timer
                player.incrementBulletSpreadLevel();
                return null; // No active effect
            });

        Ability speedy = new Ability("Speedy", "Doubles speed for 10 seconds",
            new Texture(Gdx.files.internal("assets/Abilities/Speedy/Icon_Speedy.png")),
            player -> {
                // Return active effect with timer
                return new ActiveAbility("Speedy", 10f,
                    remainingTime -> player.setSpeedMultiplier(2.0f),
                    () -> player.setSpeedMultiplier(1.0f));
            });

        // Add abilities to the list
        allAbilities.add(amocrease);
        allAbilities.add(vitality);
        allAbilities.add(damager);
        allAbilities.add(procrease);
        allAbilities.add(speedy);
    }

    // Select 3 random abilities from the pool
    public void showAbilitySelection() {
        currentSelection.clear();

        // Create a copy of all abilities so we can remove from it
        Array<Ability> availableAbilities = new Array<>(allAbilities);

        // Select 3 random abilities
        for (int i = 0; i < 3 && availableAbilities.size > 0; i++) {
            int index = random.nextInt(availableAbilities.size);
            currentSelection.add(availableAbilities.get(index));
            availableAbilities.removeIndex(index);
        }

        selectionActive = true;
    }

    // Apply the selected ability
    public void selectAbility(int index) {
        if (index >= 0 && index < currentSelection.size) {
            try {
                Ability selected = currentSelection.get(index);
                ActiveAbility effect = selected.apply(player);

                if (effect != null) {
                    activeAbilities.add(effect);
                }
            } catch (Exception e) {
                Gdx.app.error("Abilities", "Error applying ability: " + e.getMessage());
            }
            SoundManager.getInstance().play("select", 0.6f);
            selectionActive = false;
        }
    }

    // Update active ability timers
    public void update(float delta) {
        for (int i = activeAbilities.size - 1; i >= 0; i--) {
            ActiveAbility ability = activeAbilities.get(i);
            ability.update(delta);

            if (ability.isFinished()) {
                ability.end();
                activeAbilities.removeIndex(i);
            }
        }
    }

    // Render the ability selection UI
    public void render(SpriteBatch batch) {
        if (!selectionActive) return;

        // Save the current projection matrix
        Matrix4 originalMatrix = batch.getProjectionMatrix().cpy();

        // Switch to UI coordinates for consistent rendering
        OrthographicCamera uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(uiCamera.combined);

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // Increased card width and spacing
        float cardWidth = 150;  // Increased from 120
        float cardHeight = 150;
        float spacing = 60;     // Increased from 40
        float totalWidth = (cardWidth * 3) + (spacing * 2);

        // Position cards below character
        float startX = (screenWidth - totalWidth) / 2;
        float startY = screenHeight * 0.25f;

        // Display "Level Up!" text above character
        if (pixelFont != null) {
            // Draw "Level Up!" text with better styling - positioned higher
            pixelFont.setColor(1, 0.9f, 0.2f, 1); // Gold color
            pixelFont.getData().setScale(2.5f); // Make it larger
            GlyphLayout levelUpLayout = new GlyphLayout(pixelFont, "Level Up!");
            float textX = (screenWidth - levelUpLayout.width) / 2;
            float textY = screenHeight * 0.8f; // Higher position (was 0.75f)
            pixelFont.draw(batch, "Level Up!", textX, textY);

            // Draw "Choose an upgrade" text with red color
            pixelFont.setColor(1, 0.2f, 0.2f, 1); // Red color
            pixelFont.getData().setScale(1.2f); // Slightly larger than before
            GlyphLayout chooseLayout = new GlyphLayout(pixelFont, "Choose an upgrade");
            textX = (screenWidth - chooseLayout.width) / 2; // Re-center for new size
            textY = screenHeight * 0.72f; // Adjusted for new Level Up position
            pixelFont.draw(batch, "Choose an upgrade", textX, textY);

            // Reset font scale
            pixelFont.getData().setScale(1.0f);
        }

        // Display the three ability options
        for (int i = 0; i < currentSelection.size; i++) {
            Ability ability = currentSelection.get(i);
            float x = startX + (i * (cardWidth + spacing));

            // Draw background
            batch.draw(backgroundTexture, x, startY, cardWidth, cardHeight);

            // Draw icon smaller than the background with padding
            float iconSize = cardWidth * 0.6f; // Icon takes 70% of card width
            float iconX = x + (cardWidth - iconSize) / 2; // Center horizontally
            float iconY = startY + (cardHeight - iconSize) / 2; // Center vertically
            batch.draw(ability.getIcon(), iconX, iconY, iconSize, iconSize);

            // Draw frame on top
            batch.draw(frameTexture, x, startY, cardWidth, cardHeight);
        }

        // Restore original projection matrix
        batch.setProjectionMatrix(originalMatrix);
    }


    public boolean isSelectionActive() {
        return selectionActive;
    }

    // Ability class representing a particular power
    private static class Ability {
        private String name;
        private String description;
        private Texture icon;
        private AbilityEffect effect;

        public Ability(String name, String description, Texture icon, AbilityEffect effect) {
            this.name = name;
            this.description = description;
            this.icon = icon;
            this.effect = effect;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public Texture getIcon() { return icon; }

        // Apply this ability to the player
        public ActiveAbility apply(Player player) {
            return effect.apply(player);
        }
    }

    // Interface for ability effects
    private interface AbilityEffect {
        ActiveAbility apply(Player player);
    }

    // Class for temporary abilities with duration
    private static class ActiveAbility {
        private String name;
        private float duration;
        private float remainingTime;
        private ActiveEffect effect;
        private Runnable endEffect;

        public ActiveAbility(String name, float duration, ActiveEffect effect, Runnable endEffect) {
            this.name = name;
            this.duration = duration;
            this.remainingTime = duration;
            this.effect = effect;
            this.endEffect = endEffect;
        }

        public void update(float delta) {
            remainingTime -= delta;
            effect.apply(remainingTime);
        }

        public boolean isFinished() {
            return remainingTime <= 0;
        }

        public void end() {
            endEffect.run();
        }
    }

    // Interface for effects that need to be applied each frame
    private interface ActiveEffect {
        void apply(float remainingTime);
    }

    public void dispose() {
        frameTexture.dispose();
        backgroundTexture.dispose();
        for (Ability ability : allAbilities) {
            ability.getIcon().dispose();
        }
    }
}
