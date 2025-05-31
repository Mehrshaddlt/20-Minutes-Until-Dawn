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
    private Array<Ability> allAbilities;
    private BitmapFont pixelFont;
    private Array<ActiveAbility> activeAbilities;
    private WeaponsManager weaponsManager;
    private Texture frameTexture;
    private Texture backgroundTexture;
    private Random random;
    private Array<Ability> currentSelection;
    private Player player;

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
        }
        catch (Exception e) {
            System.err.println("Failed to load TTF font: " + e.getMessage());
            pixelFont = new BitmapFont();
        }
        allAbilities = new Array<>(5);
        activeAbilities = new Array<>();
        currentSelection = new Array<>(3);
        frameTexture = new Texture(Gdx.files.internal("assets/Abilities/PowerupFrame.png"));
        backgroundTexture = new Texture(Gdx.files.internal("assets/Abilities/PowerupIconBG.png"));
    }

    private void initializeAbilities() {
        Ability amocrease = new Ability("Amocrease", "Adds 5 extra ammo to all weapons",
            new Texture(Gdx.files.internal("assets/Abilities/Amocrease/Icon_Amocrease.png")),
            player -> {
                player.activateAmmoIncrease();
                weaponsManager.addExtraAmmoToCurrentWeapon("Revolver", 5);
                weaponsManager.addExtraAmmoToCurrentWeapon("Shotgun", 5);
                weaponsManager.addExtraAmmoToCurrentWeapon("SMG", 5);

                weaponsManager.addExtraAmmoCapacityToWeapon("Revolver", 5);
                weaponsManager.addExtraAmmoCapacityToWeapon("Shotgun", 5);
                weaponsManager.addExtraAmmoCapacityToWeapon("SMG", 5);

                return null;
            });


        Ability vitality = new Ability("Vitality", "Adds 1 Heart",
            new Texture(Gdx.files.internal("assets/Abilities/Vitality/Icon_Vitality.png")),
            player -> {
                player.addMaxHearts(1);
                player.activateVitality();
                player.restoreFullHealth();
                return null;
            });

        Ability damager = new Ability("Damager", "Increases damage by 25% for 10 seconds",
            new Texture(Gdx.files.internal("assets/Abilities/Damager/Icon_Damager.png")),
            player -> {
                return new ActiveAbility("Damager", 10f,
                    remainingTime -> player.setDamageMultiplier(1.25f),
                    () -> player.setDamageMultiplier(1.0f));
            });
        Ability procrease = new Ability("Procrease", "Fires an additional bullet with every shot for 10 seconds",
            new Texture(Gdx.files.internal("assets/Abilities/Procrease/Icon_Procrease.png")),
            player -> {
                player.incrementBulletSpreadLevel();
                return null;
            });

        Ability speedy = new Ability("Speedy", "Doubles speed for 10 seconds",
            new Texture(Gdx.files.internal("assets/Abilities/Speedy/Icon_Speedy.png")),
            player -> {
                return new ActiveAbility("Speedy", 10f,
                    remainingTime -> player.setSpeedMultiplier(2.0f),
                    () -> player.setSpeedMultiplier(1.0f));
            });
        allAbilities.add(amocrease);
        allAbilities.add(vitality);
        allAbilities.add(damager);
        allAbilities.add(procrease);
        allAbilities.add(speedy);
    }

    public void showAbilitySelection() {
        currentSelection.clear();
        Array<Ability> availableAbilities = new Array<>(allAbilities);
        for (int i = 0; i < 3 && availableAbilities.size > 0; i++) {
            int index = random.nextInt(availableAbilities.size);
            currentSelection.add(availableAbilities.get(index));
            availableAbilities.removeIndex(index);
        }
        selectionActive = true;
    }

    public void selectAbility(int index) {
        if (index >= 0 && index < currentSelection.size) {
            try {
                Ability selected = currentSelection.get(index);
                ActiveAbility effect = selected.apply(player);
                if (effect != null) {
                    activeAbilities.add(effect);
                }
            }
            catch (Exception e) {
                Gdx.app.error("Abilities", "Error applying ability: " + e.getMessage());
            }
            SoundManager.getInstance().play("select", 0.6f);
            selectionActive = false;
        }
    }
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
    public void render(SpriteBatch batch) {
        if (!selectionActive) return;
        Matrix4 originalMatrix = batch.getProjectionMatrix().cpy();
        OrthographicCamera uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(uiCamera.combined);
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float cardWidth = 150;
        float cardHeight = 150;
        float spacing = 60;
        float totalWidth = (cardWidth * 3) + (spacing * 2);
        float startX = (screenWidth - totalWidth) / 2;
        float startY = screenHeight * 0.25f;
        if (pixelFont != null) {
            pixelFont.setColor(1, 0.9f, 0.2f, 1);
            pixelFont.getData().setScale(2.5f);
            GlyphLayout levelUpLayout = new GlyphLayout(pixelFont, "Level Up!");
            float textX = (screenWidth - levelUpLayout.width) / 2;
            float textY = screenHeight * 0.8f;
            pixelFont.draw(batch, "Level Up!", textX, textY);
            pixelFont.setColor(1, 0.2f, 0.2f, 1);
            pixelFont.getData().setScale(1.2f);
            GlyphLayout chooseLayout = new GlyphLayout(pixelFont, "Choose an upgrade");
            textX = (screenWidth - chooseLayout.width) / 2;
            textY = screenHeight * 0.72f;
            pixelFont.draw(batch, "Choose an upgrade", textX, textY);
            pixelFont.getData().setScale(1.0f);
        }
        for (int i = 0; i < currentSelection.size; i++) {
            Ability ability = currentSelection.get(i);
            float x = startX + (i * (cardWidth + spacing));
            batch.draw(backgroundTexture, x, startY, cardWidth, cardHeight);
            float iconSize = cardWidth * 0.6f;
            float iconX = x + (cardWidth - iconSize) / 2;
            float iconY = startY + (cardHeight - iconSize) / 2;
            batch.draw(ability.getIcon(), iconX, iconY, iconSize, iconSize);
            batch.draw(frameTexture, x, startY, cardWidth, cardHeight);
        }
        batch.setProjectionMatrix(originalMatrix);
    }
    public boolean isSelectionActive() {
        return selectionActive;
    }
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
        public ActiveAbility apply(Player player) {
            return effect.apply(player);
        }
    }
    private interface AbilityEffect {
        ActiveAbility apply(Player player);
    }
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
