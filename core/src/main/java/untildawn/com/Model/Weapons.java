package untildawn.com.Model;

public class Weapons {
    // Weapon properties
    private String name;
    private int damage;
    private float fireRate;  // shots per second
    private int ammoCapacity;
    private int currentAmmo;
    private float reloadTime; // in seconds
    private int projectileCount; // number of bullets fired per shot

    public Weapons(String name) {
        this.name = name;
        setWeaponStats(name);
    }

    private void setWeaponStats(String name) {
        switch(name) {
            case "SMG":
                this.damage = 8;
                this.fireRate = 10;
                this.ammoCapacity = 24;
                this.reloadTime = 2.0f;
                this.projectileCount = 1;
                break;
            case "Revolver":
                this.damage = 20;
                this.fireRate = 2;
                this.ammoCapacity = 6;
                this.reloadTime = 1.0f;
                this.projectileCount = 1;
                break;
            case "Shotgun":
                this.damage = 10;
                this.fireRate = 1;
                this.ammoCapacity = 2;
                this.reloadTime = 1.0f;
                this.projectileCount = 4;
                break;
            default:
                this.damage = 3;
                this.fireRate = 4;
                this.ammoCapacity = 10;
                this.reloadTime = 2.0f;
                this.projectileCount = 1;
        }
        this.currentAmmo = this.ammoCapacity;
    }

    // Getters and game-related methods
    public String getName() { return name; }
    public int getDamage() { return damage; }
    public float getFireRate() { return fireRate; }
    public int getAmmoCapacity() { return ammoCapacity; }
    public int getCurrentAmmo() { return currentAmmo; }
    public int getProjectileCount() { return projectileCount; }

    public void setCurrentAmmo(int amount) {
        this.currentAmmo = amount;
    }

    public float getReloadTime() { return reloadTime; }
    public void increaseAmmoCapacity(int amount) {
        // Update the ammo capacity
        this.ammoCapacity += amount;

        // Also update current ammo to match the new capacity
        this.currentAmmo += amount;

        // Debug logs to verify the changes
        System.out.println("Weapon " + name + ": ammo capacity increased to " + ammoCapacity);
        System.out.println("Weapon " + name + ": current ammo set to " + currentAmmo);
    }
    public boolean canShoot() {
        return currentAmmo > 0;
    }
    public void setAmmoCapacity(int capacity) {
        this.ammoCapacity = capacity;
    }
    public void shoot() {
        if (canShoot()) {
            currentAmmo--;
        }
    }

    public void reload() {
        currentAmmo = ammoCapacity;
    }
}
