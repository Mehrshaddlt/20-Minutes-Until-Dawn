package untildawn.com.Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import untildawn.com.Main;
import untildawn.com.Model.SaveManager;
import untildawn.com.Model.User;
import untildawn.com.View.LeaderboardMenuView;
import untildawn.com.View.MainMenuView;

public class LeaderboardMenuController {
    private LeaderboardMenuView view;
    private User currentUser;
    private ArrayList<User> allUsers;
    private int sortMode = 0; // 0 = by score, 1 = by kills, 2 = by username
    private Main game;
    private Stage stage;

    public LeaderboardMenuController(Main game, User currentUser) {
        this.game = game;
        this.currentUser = currentUser;
        this.allUsers = loadAllUsers();
        sortUsers();
        // The view will be created in the LeaderboardMenuView constructor
    }

    public void setView(LeaderboardMenuView view) {
        this.view = view;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private ArrayList<User> loadAllUsers() {
        ArrayList<User> users = new ArrayList<>();
        try {
            FileHandle dirHandle = Gdx.files.local("users/");
            if (dirHandle.exists() && dirHandle.isDirectory()) {
                for (FileHandle file : dirHandle.list(".json")) {
                    User user = SaveManager.loadUser(file.nameWithoutExtension());
                    if (user != null && !user.isGuest()) {
                        users.add(user);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading users for leaderboard: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    public void sortUsers() {
        if (sortMode == 0) {
            // Sort by score (highest first)
            Collections.sort(allUsers, (u1, u2) -> Integer.compare(u2.getTotalScore(), u1.getTotalScore()));
        } else if (sortMode == 1) {
            // Sort by kills (highest first)
            Collections.sort(allUsers, (u1, u2) -> Integer.compare(u2.getTotalKills(), u1.getTotalKills()));
        } else {
            // Sort by username (A to Z)
            Collections.sort(allUsers, (u1, u2) -> u1.getUsername().compareToIgnoreCase(u2.getUsername()));
        }

        if (view != null) {
            view.updateLeaderboard();
        }
    }

    public void setSortMode(int mode) {
        this.sortMode = mode;
        sortUsers();
    }

    public List<User> getTopUsers(int count) {
        int size = Math.min(count, allUsers.size());
        return allUsers.subList(0, size);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public int getSortMode() {
        return sortMode;
    }

    public void goToMainMenu() {
        game.setScreen(new MainMenuView(game, currentUser));
    }

    public String getSortModeString() {
        switch(sortMode) {
            case 0: return "Highest Score";
            case 1: return "Most Kills";
            case 2: return "Username (A-Z)";
            default: return "Unknown";
        }
    }

    public ArrayList<User> getAllUsers() {
        return allUsers;
    }

    public Main getGame() {
        return game;
    }
}
