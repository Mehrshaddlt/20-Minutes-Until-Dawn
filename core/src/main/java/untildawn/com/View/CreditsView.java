package untildawn.com.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import untildawn.com.Controller.PreMenuController;
import untildawn.com.Main;
import untildawn.com.Model.AssetManager;
import untildawn.com.Model.SoundManager;

public class CreditsView implements Screen {
    private final Main game;
    private final Stage stage;
    private final PreMenuController controller;
    private Skin skin;

    // Animation properties
    private float fadeInTime = 0;
    private final float FADE_DURATION = 1.0f;
    private boolean isFadingIn = true;
    private ShapeRenderer shapeRenderer;

    public CreditsView(Main game) {
        this.game = game;
        this.controller = new PreMenuController(game);
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
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        // Title with shadow effect
        Label titleShadow = new Label("CREDITS", skin);
        titleShadow.setFontScale(2.2f);
        titleShadow.setColor(0.2f, 0.2f, 0.2f, 0.6f);
        table.add(titleShadow).padBottom(45).padRight(-4).row();

        // Creator section
        Label creatorHeader = new Label("CREATOR", skin);
        creatorHeader.setFontScale(1.4f);
        creatorHeader.setColor(0.9f, 0.9f, 0.9f, 1f);
        table.add(creatorHeader).padBottom(10).row();

        Label myName = new Label("Amirreza (Mehrshadlt) Dolati", skin);
        myName.setFontScale(1.6f);
        myName.setColor(0.9f, 0.3f, 0.3f, 1f);
        table.add(myName).padBottom(40).row();

        // Original game section
        Label inspiredHeader = new Label("ORIGINAL GAME", skin);
        inspiredHeader.setFontScale(1.4f);
        table.add(inspiredHeader).padBottom(10).row();

        Label flanne = new Label("Flanne", skin);
        flanne.setFontScale(1.3f);
        flanne.setColor(0.9f, 0.7f, 0.3f, 1f);
        table.add(flanne).padBottom(5).row();

        Label gameTitle = new Label("Developer of 20 Minutes Till Dawn", skin);
        gameTitle.setFontScale(1.1f);
        table.add(gameTitle).padBottom(40).row();

        // Assets section
        Label thanksHeader = new Label("SPECIAL THANKS", skin);
        thanksHeader.setFontScale(1.4f);
        table.add(thanksHeader).padBottom(10).row();

        Label assets = new Label("Ansimuz, AP TAs", skin);
        assets.setFontScale(1.2f);
        assets.setColor(0.5f, 0.8f, 0.9f, 1f);
        table.add(assets).padBottom(5).row();

        Label assetsDesc = new Label("For providing assets", skin);
        assetsDesc.setFontScale(1.0f);
        table.add(assetsDesc).padBottom(50).row();

        // Back button
        TextButton backButton = createRedButton("BACK");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.goBack();
            }
        });
        table.add(backButton).width(200);

        stage.addActor(table);
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
    }
}
