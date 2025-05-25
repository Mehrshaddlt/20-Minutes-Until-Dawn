package untildawn.com;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import untildawn.com.Model.AssetManager;
import untildawn.com.Model.MusicManager;
import untildawn.com.View.PreMenuView;
import untildawn.com.View.SplashScreenView;

public class Main extends Game {
    private SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Only use actual background images, specifically excluding walls.png
        AssetManager.getInstance().loadParallaxBackground(new String[]{
            "background/layers/background.png",
            "background/layers/middleground.png",
            "background/layers/middleground-no-fungus.png"
        }, 5f);
        // Set the first screen
        this.setScreen(new SplashScreenView(this));
    }

    @Override
    public void render() {
        super.render();
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
