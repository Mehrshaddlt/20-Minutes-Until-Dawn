package untildawn.com.Model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;

public class XPDrop {
    private Vector2 position;
    private Texture texture;
    private boolean active;
    private final Circle collisionCircle;
    private final float size = 25f;
    private final int xpValue = 3;

    public XPDrop(float x, float y, Texture texture) {
        this.position = new Vector2(x, y);
        this.texture = texture;
        this.active = true;
        this.collisionCircle = new Circle(x, y, size / 2);
    }
    public void render(SpriteBatch batch) {
        if (active && texture != null) {
            batch.draw(texture, position.x - size/2, position.y - size/2, size, size);
        }
    }
    public boolean isActive() {
        return active;
    }
    public void collect() {
        active = false;
    }
    public Vector2 getPosition() {
        return position;
    }
    public Circle getCollisionCircle() {
        return collisionCircle;
    }
    public int getXpValue() {
        return xpValue;
    }
}
