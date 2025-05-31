package untildawn.com.Model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

public class AssetManager {
    private static AssetManager instance;
    private Skin skin;
    private SpriteBatch batch;
    private final float TARGET_WIDTH = 1920f;
    private final float TARGET_HEIGHT = 1080f;
    private Array<BackgroundLayer> backgroundLayers;
    private Array<BackgroundProp> backgroundProps;
    private AssetManager() {
        loadAssets();
        batch = new SpriteBatch();
        backgroundLayers = new Array<>();
        backgroundProps = new Array<>();
    }

    private class BackgroundLayer {
        Texture texture;
        float scrollSpeed;
        float offsetX;

        public BackgroundLayer(Texture texture, float scrollSpeed) {
            this.texture = texture;
            this.scrollSpeed = scrollSpeed;
            this.offsetX = 0;
        }
    }

    private class BackgroundProp {
        Texture texture;
        float x;
        float y;
        float scale;
        public BackgroundProp(Texture texture, float x, float y, float scale) {
            this.texture = texture;
            this.x = x;
            this.y = y;
            this.scale = scale;
        }
    }

    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    private void loadAssets() {
        try {
            skin = new Skin(Gdx.files.internal("pixthulhu/skin/pixthulhu-ui.json"));
        } catch (Exception e) {
            System.err.println("Error loading skin: " + e.getMessage());
            skin = new Skin();
        }
    }

    public void loadParallaxBackground(String[] layerFiles, float baseSpeed) {
        try {
            disposeBackgroundLayers();
            backgroundLayers.clear();
            for (int i = 0; i < layerFiles.length; i++) {
                Texture tex = new Texture(Gdx.files.internal(layerFiles[i]));
                tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                float layerSpeed = baseSpeed * (i + 1) / 10f;
                backgroundLayers.add(new BackgroundLayer(tex, layerSpeed));
            }
        }
        catch (Exception e) {
            System.err.println("Error loading background layers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void renderBackground(float delta) {
        if (backgroundLayers.size > 0) {
            batch.begin();
            float screenWidth = Gdx.graphics.getWidth();
            float screenHeight = Gdx.graphics.getHeight();
            for (BackgroundLayer layer : backgroundLayers) {
                layer.offsetX = (layer.offsetX + layer.scrollSpeed * delta) % screenWidth;
                float imageAspectRatio = (float)layer.texture.getWidth() / layer.texture.getHeight();
                float drawWidth = screenHeight * imageAspectRatio;
                int copies = (int)Math.ceil(screenWidth / drawWidth) + 1;
                for (int i = 0; i < copies; i++) {
                    float xPos = -layer.offsetX + i * drawWidth;
                    batch.draw(layer.texture, xPos, 0, drawWidth, screenHeight);
                }
            }
            batch.end();
        }
    }

    public Skin getSkin() {
        return skin;
    }

    private void disposeBackgroundLayers() {
        if (backgroundLayers != null) {
            for (BackgroundLayer layer : backgroundLayers) {
                if (layer.texture != null) {
                    layer.texture.dispose();
                }
            }
        }

        if (backgroundProps != null) {
            for (BackgroundProp prop : backgroundProps) {
                if (prop.texture != null) {
                    prop.texture.dispose();
                }
            }
            backgroundProps.clear();
        }
    }

    public void dispose() {
        if (skin != null) {
            skin.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
        disposeBackgroundLayers();
    }
}
