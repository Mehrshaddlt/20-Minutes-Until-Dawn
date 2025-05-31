package untildawn.com.Model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

public class SaveManager {
    private static final String USERS_DIRECTORY = "users/";
    private static final String LOG_TAG = "SaveManager";
    public static void saveUser(User user) {
        try {
            String fileName = USERS_DIRECTORY + user.getUsername() + ".json";
            FileHandle fileHandle = Gdx.files.local(fileName);
            FileHandle dir = Gdx.files.local(USERS_DIRECTORY);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            String jsonString = json.prettyPrint(user);
            System.out.println("Saving user data: " + jsonString);
            fileHandle.writeString(jsonString, false);
            System.out.println("User saved successfully to: " + fileName);
        }
        catch (Exception e) {
            Gdx.app.error("SaveManager", "Failed to save user: " + e.getMessage(), e);
        }
    }
    public static User loadUser(String username) {
        try {
            String fileName = USERS_DIRECTORY + username + ".json";
            FileHandle fileHandle = Gdx.files.local(fileName);
            if (!fileHandle.exists()) {
                Gdx.app.log(LOG_TAG, "User file not found: " + fileName);
                return null;
            }
            String jsonContent = fileHandle.readString();
            if (jsonContent == null || jsonContent.isEmpty()) {
                Gdx.app.log(LOG_TAG, "Empty user file: " + fileName);
                return null;
            }
            System.out.println("Loading user data: " + jsonContent);
            Json json = new Json();
            json.setIgnoreUnknownFields(true);
            User user = json.fromJson(User.class, jsonContent);
            System.out.println("Loaded user " + username +
                " with score: " + user.getTotalScore() +
                ", highscore: " + user.getHighScore());
            return user;
        }
        catch (Exception e) {
            Gdx.app.error(LOG_TAG, "Failed to load user: " + e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }
    public static User getUserByUsername(String username) {
        return loadUser(username);
    }
}
