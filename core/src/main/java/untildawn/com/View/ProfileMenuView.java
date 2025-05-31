package untildawn.com.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import untildawn.com.Controller.ProfileMenuController;
import untildawn.com.Model.AssetManager;
import untildawn.com.Model.SoundManager;
import untildawn.com.Model.User;
import untildawn.com.Main;
import java.io.File;

public class ProfileMenuView implements Screen {
    private Main game;
    private User currentUser;
    private ProfileMenuController controller;
    private Stage stage;
    private Skin skin;

    private Image currentAvatar;
    private TextField usernameField;
    private TextField passwordField;
    private Label errorLabel;
    private String currentAvatarName;

    private static final String[] AVATAR_NAMES = {
        "Abby", "Dasher", "Diamond", "Hastur", "Hina", "Lilith",
        "Luna", "Raven", "Scarlett", "Shana", "Spark", "Yuki"
    };

    public ProfileMenuView(Main game, User currentUser) {
        this.game = game;
        this.currentUser = currentUser;
        this.controller = new ProfileMenuController(game, currentUser);
        this.currentAvatarName = currentUser.getAvatarName();

        stage = new Stage(new FitViewport(1920, 1080));
        skin = new Skin(Gdx.files.internal("pixthulhu/skin/pixthulhu-ui.json"));

        createUI();
        // Removed setupAvatarDragAndDrop();
    }

    private void createUI() {
        stage.setViewport(new ScreenViewport());
        Table mainTable = new Table();
        mainTable.setFillParent(true);

        Table leftPanel = new Table();
        Table avatarContainer = new Table();
        avatarContainer.setBackground(new TextureRegionDrawable(new TextureRegion(
            new Texture(Gdx.files.internal("T_UIPanelSelected.png")))));
        String avatarPath = "Avatars/T_" + currentAvatarName + "_Portrait.png";
        Texture avatarTexture = new Texture(Gdx.files.internal(avatarPath));
        currentAvatar = new Image(new TextureRegion(avatarTexture));
        currentAvatar.setScaling(Scaling.fit);
        avatarContainer.add(currentAvatar).size(120, 120).pad(10);

        Label selectLabel = new Label("SELECT AVATAR:", skin);
        selectLabel.setFontScale(1.1f);
        selectLabel.setColor(1.0f, 0.45f, 0.45f, 1f);

        SelectBox<String> avatarSelect = new SelectBox<>(skin);
        avatarSelect.setItems(AVATAR_NAMES);
        avatarSelect.setSelected(currentAvatarName);
        avatarSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                String selectedAvatar = avatarSelect.getSelected();
                currentAvatarName = selectedAvatar;
                String newPath = "Avatars/T_" + selectedAvatar + "_Portrait.png";
                Texture newTexture = new Texture(Gdx.files.internal(newPath));
                currentAvatar.setDrawable(new TextureRegionDrawable(new TextureRegion(newTexture)));
                controller.setAvatar(selectedAvatar);
            }
        });

        leftPanel.add(avatarContainer).size(140, 140).pad(10).center().row();
        leftPanel.add(selectLabel).padTop(15).center().row();
        leftPanel.add(avatarSelect).width(250).height(65).padTop(10).center().row();
        avatarSelect.setColor(1.0f, 0.45f, 0.45f, 1f);
        addAvatarFileSelection(leftPanel);

        Table formPanel = new Table();
        formPanel.center();

        Label titleLabel = new Label("PROFILE", skin);
        titleLabel.setFontScale(2f);
        titleLabel.setColor(1.0f, 0.45f, 0.45f, 1f);

        Label usernameLabel = new Label("CHANGE USERNAME", skin);
        usernameLabel.setFontScale(1.1f);
        usernameField = new TextField(currentUser.getUsername(), skin);
        usernameField.setMessageText("New username (min 3 characters)");

        Label passwordLabel = new Label("CHANGE PASSWORD", skin);
        passwordLabel.setFontScale(1.1f);
        passwordField = new TextField("", skin);
        passwordField.setMessageText("New password (min 6 characters)");
        passwordField.setPasswordCharacter('*');
        passwordField.setPasswordMode(true);

        errorLabel = new Label("", skin);
        errorLabel.setColor(1.0f, 0.45f, 0.45f, 1f);

        TextButton saveButton = createRedButton("SAVE CHANGES");
        saveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                saveChanges();
            }
        });

        TextButton deleteButton = createRedButton("DELETE ACCOUNT");
        deleteButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                confirmDelete();
            }
        });

        TextButton backButton = createRedButton("BACK");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.backToMainMenu();
            }
        });

        formPanel.add(titleLabel).padBottom(40).colspan(2).row();
        formPanel.add(usernameLabel).left().padBottom(5).row();
        formPanel.add(usernameField).width(400).height(80).padBottom(20).row();
        formPanel.add(passwordLabel).left().padBottom(5).row();
        formPanel.add(passwordField).width(600).height(80).padBottom(20).row();
        formPanel.add(saveButton).width(500).height(100).padBottom(10).row();
        formPanel.add(errorLabel).height(35).padBottom(80).row();
        formPanel.add(deleteButton).width(600).height(100).padBottom(10).row();
        formPanel.add(backButton).width(400).height(100).row();

        mainTable.add(leftPanel).width(250).fillY().padLeft(50);
        mainTable.add(formPanel).expand().fill().padRight(50);

        stage.addActor(mainTable);
    }

    private TextButton createRedButton(String text) {
        TextButton button = new TextButton(text, skin);
        button.setColor(1.0f, 0.45f, 0.45f, 1f);
        button.getLabel().setFontScale(1.1f);
        return button;
    }

    private void saveChanges() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        boolean changes = false;

        if (!username.equals(currentUser.getUsername())) {
            if (controller.updateUsername(username)) {
                changes = true;
            }
            else {
                errorLabel.setText("Username must be unique");
                return;
            }
        }

        if (!password.isEmpty()) {
            if (controller.updatePassword(password)) {
                changes = true;
                passwordField.setText("");
            }
            else {
                errorLabel.setText("Password must match the Criteria");
                return;
            }
        }

        if (changes) {
            errorLabel.setText("Changes saved successfully!");
        }
    }

    private void confirmDelete() {
        Dialog dialog = new Dialog("", skin);
        dialog.text("Are you sure you want to delete your account?");

        dialog.button("Yes", true).button("No", false);
        dialog.setModal(true);

        TextButton yesButton = (TextButton)dialog.getButtonTable().getCells().first().getActor();
        yesButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.deleteAccount();
                dialog.hide();
            }
        });

        dialog.show(stage);
    }

    private void addAvatarFileSelection(Table leftPanel) {
        TextButton chooseAvatarButton = new TextButton("Choose Avatar", skin);
        chooseAvatarButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                openAvatarFileChooser();
            }
        });
        leftPanel.add(chooseAvatarButton).width(350).height(90).padTop(10).center().row();
        chooseAvatarButton.setColor(1.0f, 0.45f, 0.45f, 1f);
    }

    private void openAvatarFileChooser() {
        Gdx.app.postRunnable(() -> {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(1280, 720);
            }
            javax.swing.SwingUtilities.invokeLater(() -> {
                javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
                chooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
                int result = chooser.showOpenDialog(null);
                if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    setCustomAvatarFromFile(file);
                }
                Gdx.app.postRunnable(() -> {
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                });
            });
        });
    }

    private void setCustomAvatarFromFile(File file) {
        if (file == null) return;
        Gdx.app.postRunnable(() -> {
            try {
                Texture newTexture = new Texture(Gdx.files.absolute(file.getAbsolutePath()));
                currentAvatar.setDrawable(new TextureRegionDrawable(new TextureRegion(newTexture)));
                controller.setCustomAvatar(file.getAbsolutePath());
            } catch (Exception e) {
                // Optionally show error
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Check for dropped file (LWJGL3 drag-and-drop)
        String droppedFile = game.consumeDroppedAvatarFile();
        if (droppedFile != null) {
            setCustomAvatarFromFile(new File(droppedFile));
        }

        AssetManager.getInstance().renderBackground(delta);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
