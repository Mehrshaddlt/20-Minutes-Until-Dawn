package untildawn.com.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import untildawn.com.Controller.MainMenuController;
import untildawn.com.Main;
import untildawn.com.Model.AssetManager;
import untildawn.com.Model.SoundManager;
import untildawn.com.Model.User;

public class MainMenuView implements Screen {
    private final Main game;
    private final Stage stage;
    private final MainMenuController controller;
    private final User user;
    private Skin skin;
    private final ShapeRenderer shapeRenderer;

    // Add these fields
    private Image avatar;
    private Texture avatarTexture;

    // Animation properties
    private float fadeInTime = 0;
    private final float FADE_DURATION = 1.0f;
    private boolean isFadingIn = true;

    public MainMenuView(Main game, User user) {
        this.game = game;
        this.user = user;
        this.controller = new MainMenuController(game);
        this.controller.setUser(user);
        this.stage = new Stage(new ScreenViewport());
        this.skin = AssetManager.getInstance().getSkin();
        this.shapeRenderer = new ShapeRenderer();

        createUI();
        Gdx.input.setInputProcessor(stage);

        // Start with fade effect
        stage.getRoot().getColor().a = 0;
    }

    private TextButton createRedButton(String text) {
        TextButton button = new TextButton(text, skin);
        button.setColor(1.0f, 0.45f, 0.45f, 1f);
        return button;
    }

    private void createUI() {
        // Main container that splits the screen horizontally
        Table mainTable = new Table();
        mainTable.setFillParent(true);

        // Left panel for profile information
        Table profilePanel = new Table();

        // Avatar with frame
        Table avatarContainer = new Table();
        avatarContainer.setBackground(new TextureRegionDrawable(new TextureRegion(
            new Texture(Gdx.files.internal("assets/T_UIPanelSelected.png")))));

        try {
            if (user.getAvatarPath() != null && !user.getAvatarPath().isEmpty()) {
                avatarTexture = new Texture(Gdx.files.internal(user.getAvatarPath()));
            } else {
                avatarTexture = new Texture(Gdx.files.internal("Avatars/default.png"));
            }
        } catch (Exception e) {
            Pixmap pixmap = new Pixmap(120, 120, Pixmap.Format.RGBA8888);
            pixmap.setColor(0.5f, 0.5f, 0.5f, 1);
            pixmap.fill();
            pixmap.setColor(0.7f, 0.7f, 0.7f, 1);
            pixmap.fillCircle(60, 60, 55);
            avatarTexture = new Texture(pixmap);
            pixmap.dispose();
        }

        avatar = new Image(new TextureRegion(avatarTexture));
        avatar.setScaling(Scaling.fit);
        avatarContainer.add(avatar).size(120, 120).pad(10);

        // User information
        Label nameLabel = new Label("" + user.getUsername(), skin);
        nameLabel.setFontScale(1.2f);

        int score = user.isGuest() ? 0 : user.getTotalScore(); // Placeholder
        Label scoreLabel = new Label("Score: " + score, skin);
        scoreLabel.setFontScale(1.2f);

        // Logout button
        TextButton logoutButton = createRedButton("LOGOUT");
        logoutButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.logout();
            }
        });

        // Stack profile elements vertically on the left
        profilePanel.add(avatarContainer).size(140, 140).pad(10).center().row();
        profilePanel.add(nameLabel).padTop(15).center().row();
        profilePanel.add(scoreLabel).padTop(10).center().row();
        profilePanel.add(logoutButton).padTop(30).width(250).center();

        // Right panel for menu buttons
        Table menuPanel = new Table();
        menuPanel.center();
        // Game title
        Label titleLabel = new Label("MAIN MENU", skin);
        titleLabel.setFontScale(2f);
        titleLabel.setColor(0.9f, 0.3f, 0.3f, 1f);

        // Menu buttons
        TextButton resumeButton = createRedButton("RESUME SAVED GAME");
        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.resumeGame();
            }
        });

        TextButton preGameButton = createRedButton("PRE-GAME");
        preGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.openPreGame();
            }
        });

        TextButton profileButton = createRedButton("PROFILE");
        profileButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.openProfile();
            }
        });

        TextButton scoreboardButton = createRedButton("SCOREBOARD");
        scoreboardButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.openScoreboard();
            }
        });

        TextButton talentsButton = createRedButton("TALENTS");
        talentsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.openTalents();
            }
        });

        TextButton settingsButton = createRedButton("SETTINGS");
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.openSettings();
            }
        });

        // Add elements to the menu panel with more spacing
        menuPanel.add(titleLabel).padBottom(50).row();
        menuPanel.add(resumeButton).width(650).padBottom(25).row();
        menuPanel.add(preGameButton).width(300).padBottom(25).row();
        menuPanel.add(profileButton).width(300).padBottom(25).row();
        menuPanel.add(scoreboardButton).width(400).padBottom(25).row();
        menuPanel.add(talentsButton).width(300).padBottom(25).row();
        menuPanel.add(settingsButton).width(300).padBottom(25).row();

        // Split the screen: profile on left, menu on right
        mainTable.add(profilePanel).width(250).fillY().padLeft(50);
        mainTable.add(menuPanel).expand().fill().padRight(50);

        stage.addActor(mainTable);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render background
        AssetManager.getInstance().renderBackground(delta);

        // Handle fade-in animation
        if (isFadingIn) {
            fadeInTime += delta;
            float progress = Math.min(fadeInTime / FADE_DURATION, 1.0f);

            // Fade IN stage UI elements
            stage.getRoot().getColor().a = progress;

            // Fade OUT overlay
            float overlayAlpha = 1.0f - progress;

            if (overlayAlpha > 0) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(0, 0, 0, overlayAlpha);
                shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                shapeRenderer.end();

                Gdx.gl.glDisable(GL20.GL_BLEND);
            } else {
                isFadingIn = false;
            }
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        stage.dispose();
        shapeRenderer.dispose();

        // Dispose the avatar texture
        if (avatarTexture != null) {
            avatarTexture.dispose();
        }

        // Dispose the frame texture
        if (avatar != null && avatar.getParent() instanceof Table) {
            Table container = (Table) avatar.getParent();
            if (container.getBackground() instanceof TextureRegionDrawable) {
                Texture frameTexture = ((TextureRegionDrawable)container.getBackground()).getRegion().getTexture();
                if (frameTexture != null) {
                    frameTexture.dispose();
                }
            }
        }
    }
}
