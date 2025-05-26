package untildawn.com;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import untildawn.com.Model.AssetManager;
import untildawn.com.View.SplashScreenView;

public class Main extends Game {
    private SpriteBatch batch;
    private boolean wasActive = true;

    @Override
    public void create() {
        batch = new SpriteBatch();
        // Force fullscreen mode
        setFullscreen();

        // Only use actual background images, specifically excluding walls.png
        AssetManager.getInstance().loadParallaxBackground(new String[]{
            "background/layers/background.png",
            "background/layers/middleground.png",
            "background/layers/middleground-no-fungus.png"
        }, 5f);
        // Set the first screen
        this.setScreen(new SplashScreenView(this));
    }

    private void setFullscreen() {
        // Force fullscreen mode
        Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
        Gdx.graphics.setFullscreenMode(currentMode);

        // Set continuous rendering to maintain smooth gameplay
        Gdx.graphics.setContinuousRendering(true);

        // Set window to be undecorated (no title bar)
        System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
    }

    @Override
    public void render() {
        // Handle Alt+Tab attempt to regain focus
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ALT_LEFT) &&
            Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.TAB)) {
            setFullscreen();
        }

        super.render();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        // Re-apply fullscreen if window is resized
        setFullscreen();
    }

    @Override
    public void resume() {
        // Called when the application regains focus
        setFullscreen();
        super.resume();
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }

    public SpriteBatch getBatch() {
        return batch;
    }
}
