package untildawn.com.Model;

public class SaveGameState {
    private final int remainingTime;
    private final int playerHealth;
    private final float playerX;
    private final float playerY;
    private final String currentWeapon;
    private final int[] ammoCount;

    public SaveGameState(int remainingTime, int playerHealth, float playerX, float playerY,
                         String currentWeapon, int[] ammoCount) {
        this.remainingTime = remainingTime;
        this.playerHealth = playerHealth;
        this.playerX = playerX;
        this.playerY = playerY;
        this.currentWeapon = currentWeapon;
        this.ammoCount = ammoCount;
    }
    public int getRemainingTime() {
        return remainingTime;
    }
    public int getPlayerHealth() {
        return playerHealth;
    }
    public float getPlayerX() {
        return playerX;
    }
    public float getPlayerY() {
        return playerY;
    }
    public String getCurrentWeapon() {
        return currentWeapon;
    }
    public int[] getAmmoCount() {
        return ammoCount;
    }
}
