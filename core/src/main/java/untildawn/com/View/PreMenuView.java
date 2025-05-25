package untildawn.com.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import untildawn.com.Controller.PreMenuController;
import untildawn.com.Main;
import untildawn.com.Model.AssetManager;
import untildawn.com.Model.MusicManager;
import untildawn.com.Model.SoundManager;

public class PreMenuView implements Screen {
    private final Main game;
    private final Stage stage;
    private final PreMenuController controller;
    private Skin skin;

    private float fadeInTime = 0;
    private final float FADE_DURATION = 1.0f;
    private boolean isFadingIn = true;
    private ShapeRenderer shapeRenderer;

    public PreMenuView(Main game) {
        this.game = game;
        this.controller = new PreMenuController(game);
        this.stage = new Stage(new ScreenViewport());
        this.shapeRenderer = new ShapeRenderer();

        // Get skin from AssetManager
        this.skin = AssetManager.getInstance().getSkin();

        createUI();
        Gdx.input.setInputProcessor(stage);

        stage.getRoot().getColor().a = 0;
    }

    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);

        // Game title
        TextButton titleLabel = new TextButton("20 MINUTES TILL DAWN", skin);
        titleLabel.setColor(0.9f, 0.3f, 0.3f, 1f);
        table.add(titleLabel).padBottom(50).row();

        TextButton registerButton = createRedButton("REGISTER");
        registerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.openRegisterMenu();
            }
        });

        TextButton loginButton = createRedButton("LOGIN");
        loginButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                game.setScreen(new LoginMenuView(game));
            }
        });

        TextButton creditsButton = createRedButton("CREDITS");
        creditsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.openCredits();
            }
        });

        TextButton exitButton = createRedButton("EXIT");
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.exitGame();
            }
        });

        table.add(registerButton).width(300).padBottom(20).row();
        table.add(loginButton).width(250).padBottom(20).row();
        table.add(creditsButton).width(250).padBottom(20).row();
        table.add(exitButton).width(250).row();

        stage.addActor(table);
    }
    // Helper method to create a red button
    private TextButton createRedButton(String text) {
        TextButton button = new TextButton(text, skin);
        // Bright red with slight pink tint
        button.setColor(1.0f, 0.45f, 0.45f, 1f);
        return button;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        MusicManager.getInstance().playMusic("Menu 1");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render the background first
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
                // Enable blending
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                // Draw black rectangle with fading alpha
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(0, 0, 0, overlayAlpha);
                shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                shapeRenderer.end();

                Gdx.gl.glDisable(GL20.GL_BLEND);
            } else {
                isFadingIn = false;
            }
        }

        // Then render the UI
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
        shapeRenderer.dispose();
        // Don't dispose skin here as AssetManager handles it
    }
}
