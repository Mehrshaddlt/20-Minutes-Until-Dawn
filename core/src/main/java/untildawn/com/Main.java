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
    private boolean isFullscreen = true;
    private String droppedAvatarFile = null;

    @Override
    public void create() {
        batch = new SpriteBatch();
        setFullscreen();

        AssetManager.getInstance().loadParallaxBackground(new String[]{
            "background/layers/background.png",
            "background/layers/middleground.png",
            "background/layers/middleground-no-fungus.png"
        }, 5f);
        this.setScreen(new SplashScreenView(this));
    }

    private void setFullscreen() {
        Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
        Gdx.graphics.setFullscreenMode(currentMode);
        Gdx.graphics.setContinuousRendering(true);
        System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
    }
    public void setDroppedAvatarFile(String filePath) {
        this.droppedAvatarFile = filePath;
    }

    public String consumeDroppedAvatarFile() {
        String file = droppedAvatarFile;
        droppedAvatarFile = null;
        return file;
    }
    @Override
    public void render() {
        // Handle Alt+Tab attempt to regain focus
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ALT_LEFT) &&
            Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.TAB)) {
            setFullscreen();
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F11)) {
            isFullscreen = !isFullscreen;
            if (isFullscreen) {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            } else {
                Gdx.graphics.setWindowedMode(1920, 1080); // or your preferred window size
            }
        }
        super.render();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (isFullscreen) {
            setFullscreen();
        }
    }

    @Override
    public void resume() {
        // Called when the application regains focus
        if (isFullscreen) {
            setFullscreen();
        }
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
