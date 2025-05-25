package untildawn.com.Model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private String securityQuestion;
    private String securityAnswer;
    private boolean isGuest;
    private User currentUser;
    private String avatarPath;
    private String selectedHero;
    private String selectedWeapon;

    // Game statistics
    private int totalGamesPlayed = 0;
    private int gamesWon = 0;
    private int totalKills = 0;
    private int highScore = 0;
    private int totalScore = 0;
    // Static list to store users
    private static List<User> users = new ArrayList<>();

    // Guest constructor
    public User() {
        this.isGuest = true;
    }

    // Registered user constructor
    public User(String username, String password, String securityQuestion, String securityAnswer) {
        this.username = username;
        this.password = password;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.isGuest = false;
        this.avatarPath = "Avatars/T_Abby_Portrait.png";
    }

    // Simple constructor with just username and password
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.isGuest = false;
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isGuest() { return isGuest; }
    public void setIsGuest(boolean isGuest) { this.isGuest = isGuest; }

    public String getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }

    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }

    // Static methods for user management
    public static boolean checkUserExists(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public static User getUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public static boolean saveUser(User user) {
        // Check if username already exists
        if (checkUserExists(user.getUsername())) {
            return false;
        }

        // Add to list of users
        users.add(user);
        return true;
    }
    public int getTotalGamesPlayed() { return totalGamesPlayed; }
    public void setTotalGamesPlayed(int totalGamesPlayed) { this.totalGamesPlayed = totalGamesPlayed; }

    public int getGamesWon() { return gamesWon; }
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }

    public int getTotalKills() { return totalKills; }
    public void setTotalKills(int totalKills) { this.totalKills = totalKills; }

    public int getHighScore() { return highScore; }
    public void setHighScore(int highScore) { this.highScore = highScore; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
    public User getCurrentUser() {
        return currentUser;
    }
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public String getAvatarPath() {
        return avatarPath;
    }
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public static boolean isUsernameTaken(String username, User currentUser) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user != currentUser) {
                return true;
            }
        }
        return false;
    }

    public static void removeUser(User user) {
        users.remove(user);
    }

    public String getAvatarName() {
        if (avatarPath == null || avatarPath.isEmpty()) {
            return "Abby";
        }
        // "Avatars/T_Name_Portrait.png" format
        String filename = avatarPath.substring(avatarPath.lastIndexOf("/") + 1);
        return filename.substring(2, filename.indexOf("_Portrait"));
    }

    public void setAvatarName(String name) {
        this.avatarPath = "Avatars/T_" + name + "_Portrait.png";
    }

    public String getSelectedHero() {
        return selectedHero != null ? selectedHero : "Shana";
    }

    public void setSelectedHero(String selectedHero) {
        this.selectedHero = selectedHero;
    }

    public String getSelectedWeapon() {
        return selectedWeapon != null ? selectedWeapon : "Revolver";
    }

    public void setSelectedWeapon(String selectedWeapon) {
        this.selectedWeapon = selectedWeapon;
    }
}
