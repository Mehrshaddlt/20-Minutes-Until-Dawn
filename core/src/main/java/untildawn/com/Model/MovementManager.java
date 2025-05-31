package untildawn.com.Model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class MovementManager {
    private final Array<TextureRegion> dasherRunFrames;
    private final Array<TextureRegion> diamondRunFrames;
    private final Array<TextureRegion> lilithRunFrames;
    private final Array<TextureRegion> scarlettRunFrames;
    private final Array<TextureRegion> shanaRunFrames;
    private final Array<TextureRegion> diamondWalkFrames;
    private final Array<TextureRegion> lilithWalkFrames;
    private final Array<TextureRegion> shanaWalkFrames;

    public MovementManager() {
        dasherRunFrames = new Array<>();
        diamondRunFrames = new Array<>();
        lilithRunFrames = new Array<>();
        scarlettRunFrames = new Array<>();
        shanaRunFrames = new Array<>();
        diamondWalkFrames = new Array<>();
        lilithWalkFrames = new Array<>();
        shanaWalkFrames = new Array<>();
        loadRunningFrames();
        loadWalkingFrames();
    }

    private void loadRunningFrames() {
        for (int i = 0; i < 4; i++) {
            dasherRunFrames.add(loadTextureRegion("Heroes/Running/Run_" + i + "_Dasher.png"));
            diamondRunFrames.add(loadTextureRegion("Heroes/Running/Run_" + i + "_Diamond.png"));
            lilithRunFrames.add(loadTextureRegion("Heroes/Running/Run_" + i + "_Lilith.png"));
            scarlettRunFrames.add(loadTextureRegion("Heroes/Running/Run_" + i + "_Scarlett.png"));
            shanaRunFrames.add(loadTextureRegion("Heroes/Running/Run_" + i + "_Shana.png"));
        }
    }

    private void loadWalkingFrames() {
        for (int i = 0; i < 8; i++) {
            diamondWalkFrames.add(loadTextureRegion("Heroes/Walk/Walk_" + i + "_Diamond.png"));
            lilithWalkFrames.add(loadTextureRegion("Heroes/Walk/Walk_" + i + "_Lilith.png"));
            shanaWalkFrames.add(loadTextureRegion("Heroes/Walk/Walk_" + i + "_Shana.png"));
        }
    }

    private TextureRegion loadTextureRegion(String filename) {
        return new TextureRegion(new Texture(Gdx.files.internal(filename)));
    }

    public Array<TextureRegion> getRunFrames(String character) {
        switch (character.toLowerCase()) {
            case "dasher":
                return dasherRunFrames;
            case "diamond":
                return diamondRunFrames;
            case "lilith":
                return lilithRunFrames;
            case "scarlett":
                return scarlettRunFrames;
            case "shana":
                return shanaRunFrames;
            default:
                return diamondRunFrames;
        }
    }

    public Array<TextureRegion> getWalkFrames(String character) {
        switch (character.toLowerCase()) {
            case "diamond":
                return diamondWalkFrames;
            case "lilith":
                return lilithWalkFrames;
            case "shana":
                return shanaWalkFrames;
            case "dasher":
                return dasherRunFrames;
            case "scarlett":
                return scarlettRunFrames;
            default:
                return diamondWalkFrames;
        }
    }

    public void dispose() {
        disposeFrames(dasherRunFrames);
        disposeFrames(diamondRunFrames);
        disposeFrames(lilithRunFrames);
        disposeFrames(scarlettRunFrames);
        disposeFrames(shanaRunFrames);
        disposeFrames(diamondWalkFrames);
        disposeFrames(lilithWalkFrames);
        disposeFrames(shanaWalkFrames);
    }

    private void disposeFrames(Array<TextureRegion> frames) {
        for (TextureRegion frame : frames) {
            if (frame.getTexture() != null) {
                frame.getTexture().dispose();
            }
        }
    }
}
