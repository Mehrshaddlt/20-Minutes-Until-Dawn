package untildawn.com.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import untildawn.com.Main;
import untildawn.com.Model.AssetManager;

public class SplashScreenView implements Screen {
    private final Main game;
    private final Stage stage;
    private Skin skin;

    // Elements to display
    private Image logoImage;
    private Image authorImage;

    // State management
    private int currentState = 0; // 0: logo, 1: second logo
    private float stateTime = 0;
    private final float FADE_IN_TIME = 1.0f;
    private final float DISPLAY_TIME = 2.0f;
    private final float FADE_OUT_TIME = 1.0f;
    private final float TOTAL_STATE_TIME = FADE_IN_TIME + DISPLAY_TIME + FADE_OUT_TIME;

    public SplashScreenView(Main game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        this.skin = AssetManager.getInstance().getSkin();

        setupSplashElements();
        Gdx.input.setInputProcessor(stage);
    }

    private void setupSplashElements() {
        // Create a Stack to overlay both images at the same central position
        com.badlogic.gdx.scenes.scene2d.ui.Stack imageStack = new com.badlogic.gdx.scenes.scene2d.ui.Stack();
        imageStack.setFillParent(true);

        // Create textures
        Texture logoTexture = new Texture(Gdx.files.internal("Logo1.png"));
        Texture authorTexture = new Texture(Gdx.files.internal("Logo2.png"));

        // Create images preserving original dimensions
        logoImage = new Image(logoTexture);
        authorImage = new Image(authorTexture);

        // Set origin to center for both images
        logoImage.setOrigin(Align.center);
        authorImage.setOrigin(Align.center);

        // Start both invisible
        logoImage.getColor().a = 0;
        authorImage.getColor().a = 0;

        // Center the images within the stack
        Table logoTable = new Table();
        logoTable.center();
        logoTable.setFillParent(true);
        logoTable.add(logoImage).center();

        Table authorTable = new Table();
        authorTable.center();
        authorTable.setFillParent(true);
        authorTable.add(authorImage).center();

        // Add both tables to the stack (they will overlay each other)
        imageStack.add(logoTable);
        imageStack.add(authorTable);

        // Add the stack to the stage
        stage.addActor(imageStack);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stateTime += delta;
        updateCurrentElement(delta);

        stage.act(delta);
        stage.draw();

        // Check for transition to next state
        if (stateTime >= TOTAL_STATE_TIME) {
            currentState++;
            stateTime = 0;

            // If we're done with all states, move to the game
            if (currentState > 1) {
                game.setScreen(new PreMenuView(game));
            }
        }
    }

    private void updateCurrentElement(float delta) {
        // Reset all elements to invisible
        logoImage.getColor().a = 0;
        authorImage.getColor().a = 0;

        // Calculate alpha based on current time within state
        float alpha = 0;
        if (stateTime < FADE_IN_TIME) {
            // Fading in
            alpha = stateTime / FADE_IN_TIME;
        } else if (stateTime < FADE_IN_TIME + DISPLAY_TIME) {
            // Full display
            alpha = 1.0f;
        } else {
            // Fading out
            float fadeOutProgress = stateTime - (FADE_IN_TIME + DISPLAY_TIME);
            alpha = 1.0f - (fadeOutProgress / FADE_OUT_TIME);
        }

        // Clamp alpha between 0 and 1
        alpha = Math.max(0, Math.min(1, alpha));

        // Apply alpha to current element
        switch (currentState) {
            case 0:
                logoImage.getColor().a = alpha;
                break;
            case 1:
                authorImage.getColor().a = alpha;
                break;
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
}
