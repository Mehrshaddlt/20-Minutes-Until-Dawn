package untildawn.com.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import untildawn.com.Controller.PreGameMenuController;
import untildawn.com.Main;
import untildawn.com.Model.*;

public class PreGameMenuView implements Screen {
    private final Main game;
    private final Stage stage;
    private final Skin skin;
    private final IdleManager idleManager;
    private String selectedHero = "Shana";
    private float animationTime = 0f;
    private final float frameDuration = 0.15f;
    private HeroIdleActor heroIdleActor;
    private ImageButton selectedHeroButton;
    private final PreGameMenuController controller;
    private String selectedWeapon = "SMG";
    private WeaponDisplayActor weaponDisplayActor;

    public PreGameMenuView(Main game, User currentUser) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        this.skin = new Skin(Gdx.files.internal("pixthulhu/skin/pixthulhu-ui.json"));
        this.idleManager = new IdleManager();
        this.controller = new PreGameMenuController(game, currentUser);

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        // --- Left section: Hero selection ---
        Table leftSection = new Table();

        // Create heroIdleActor ONCE
        heroIdleActor = new HeroIdleActor();
        heroIdleActor.setSize(120, 180);

        // Hero animation inside panel
        Table heroPanel = new Table();
        heroPanel.setBackground(new TextureRegionDrawable(new TextureRegion(
            new Texture(Gdx.files.internal("T_UIPanelSelected.png")))));
        heroPanel.add(heroIdleActor).size(120, 180).pad(10);

        leftSection.add(heroPanel).size(140, 200).pad(10).center().row();

        Label heroTitle = new Label("SELECT HERO", skin);
        heroTitle.setFontScale(1.1f);
        heroTitle.setColor(1.0f, 0.45f, 0.45f, 1f);
        leftSection.add(heroTitle).padTop(15).center().row();

        final SelectBox<String> heroSelect = new SelectBox<>(skin);
        heroSelect.setItems(IdleManager.HERO_NAMES);
        heroSelect.setSelected(selectedHero);
        heroSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                selectedHero = heroSelect.getSelected();
                animationTime = 0f;
                heroIdleActor.setHero(selectedHero);
            }
        });
        leftSection.add(heroSelect).width(250).height(65).padTop(10).center();

// --- Center section: Hero animation and buttons ---
        Table centerSection = new Table();

// Title at the top
        Label titleLabel = new Label("PRE-GAME MENU", skin);
        titleLabel.setFontScale(2f);
        titleLabel.setColor(0.9f, 0.3f, 0.3f, 1f);
        centerSection.add(titleLabel).padBottom(35).center().row();

// Timer selection
        Label timerTitle = new Label("GAME DURATION", skin);
        timerTitle.setFontScale(1.1f);
        timerTitle.setColor(1.0f, 0.45f, 0.45f, 1f);
        centerSection.add(timerTitle).padTop(10).center().row();

        final SelectBox<String> timerSelect = new SelectBox<>(skin);
        timerSelect.setItems("20 Minutes", "10 Minutes", "5 Minutes", "2 Minutes");
        timerSelect.setSelected("20 Minutes");
        timerSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.saveSelectedTime(timerSelect.getSelected());
            }
        });
        centerSection.add(timerSelect).width(250).height(65).padTop(10).padBottom(20).center().row();

// Start Game button
        TextButton startGameBtn = new TextButton("START GAME", skin);
        startGameBtn.setColor(1.0f, 0.45f, 0.45f, 1f);
        startGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.saveSelectedHero(selectedHero);
                controller.startGame();
            }
        });
        centerSection.add(startGameBtn).width(350).padBottom(15).center().row();

// Back button
        TextButton backBtn = new TextButton("BACK", skin);
        backBtn.setColor(1.0f, 0.45f, 0.45f, 1f);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.backToMainMenu();
            }
        });
        centerSection.add(backBtn).width(200).center();

        // --- Right section: Weapon selection ---
        Table rightSection = new Table();

        // Create weapon display actor
        weaponDisplayActor = new WeaponDisplayActor();
        weaponDisplayActor.setSize(120, 180);

        // Weapon image inside panel
        Table weaponPanel = new Table();
        weaponPanel.setBackground(new TextureRegionDrawable(new TextureRegion(
            new Texture(Gdx.files.internal("T_UIPanelSelected.png")))));
        weaponPanel.add(weaponDisplayActor).size(120, 180).pad(10);

        rightSection.add(weaponPanel).size(140, 200).pad(10).center().row();

        Label weaponTitle = new Label("SELECT WEAPON", skin);
        weaponTitle.setFontScale(1.1f);
        weaponTitle.setColor(1.0f, 0.45f, 0.45f, 1f);
        rightSection.add(weaponTitle).padTop(15).center().row();

        final SelectBox<String> weaponSelect = new SelectBox<>(skin);
        weaponSelect.setItems("SMG", "Revolver", "Shotgun");
        weaponSelect.setSelected(selectedWeapon);
        weaponSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                selectedWeapon = weaponSelect.getSelected();
                weaponDisplayActor.setWeapon(selectedWeapon);
                controller.saveSelectedWeapon(selectedWeapon);
            }
        });
        rightSection.add(weaponSelect).width(250).height(65).padTop(10).center();

        // --- Add all three sections to the main table ---
        mainTable.add(leftSection).width(300).expandY().left();
        mainTable.add(centerSection).width(400).expandY().expandX();
        mainTable.add(rightSection).width(300).expandY().right();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Clear the screen properly
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render background if you have one
        AssetManager.getInstance().renderBackground(delta);

        animationTime += delta;
        heroIdleActor.setAnimationTime(animationTime);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
    private class WeaponDisplayActor extends Actor {
        private String weapon = selectedWeapon;
        private TextureRegion currentTexture;
        private final WeaponsManager weaponsManager;

        public WeaponDisplayActor() {
            weaponsManager = new WeaponsManager();
            updateWeaponTexture();
        }

        public void setWeapon(String weapon) {
            this.weapon = weapon;
            updateWeaponTexture();
        }

        private void updateWeaponTexture() {
            currentTexture = weaponsManager.getWeaponTexture(weapon);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (currentTexture != null) {
                // Draw centered and scaled appropriately
                float width = getWidth();
                float height = getHeight();
                batch.draw(currentTexture, getX(), getY(), width, height);
            }
        }
    }
    // Custom Actor for hero idle animation
    private class HeroIdleActor extends Actor {
        private String hero = selectedHero;
        private float animTime = 0f;

        public void setHero(String hero) {
            this.hero = hero;
        }

        public void setAnimationTime(float t) {
            this.animTime = t;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            Array<TextureRegion> frames = idleManager.getIdleFrames(hero);
            if (frames != null && frames.size > 0) {
                int frameIndex = (int)(animTime / frameDuration) % frames.size;
                TextureRegion frame = frames.get(frameIndex);

                // Draw centered and scaled appropriately
                float width = getWidth();
                float height = getHeight();
                batch.draw(frame, getX(), getY(), width, height);
            }
        }
    }
}
