package untildawn.com.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import untildawn.com.Controller.SettingsMenuController;
import untildawn.com.Main;
import untildawn.com.Model.AssetManager;
import untildawn.com.Model.SoundManager;
import untildawn.com.Model.User;

public class SettingsMenuView implements Screen {
    private final Main game;
    private final User currentUser;
    private Stage stage;
    private Skin skin;
    private SettingsMenuController controller;

    // UI elements
    private SelectBox<String> musicSelect;
    private Slider volumeSlider;
    private CheckBox sfxCheckbox;
    private Label waitingForInput;
    private String currentRebinding;
    private Table dPadTable;
    private Table autoaimTable;
    private TextButton autoaimButton;

    public SettingsMenuView(Main game, User currentUser) {
        this.game = game;
        this.currentUser = currentUser;
        controller = new SettingsMenuController(game, currentUser);
        stage = new Stage(new ScreenViewport());
        skin = AssetManager.getInstance().getSkin();

        createUI();
        Gdx.input.setInputProcessor(stage);
    }

    private TextButton createRedButton(String text) {
        TextButton button = new TextButton(text, skin, "default");
        button.getLabel().setFontScale(1.2f);
        return button;
    }

    private TextButton createKeyBindButton(final String bindId, String currentKey) {
        TextButton button = createRedButton(currentKey);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                currentRebinding = bindId;
                waitingForInput.setText("Press key for " + getActionName(bindId));
                waitingForInput.setVisible(true);
            }
        });
        return button;
    }

    private String getActionName(String bindId) {
        switch(bindId) {
            case "UP": return "Move Up";
            case "LEFT": return "Move Left";
            case "DOWN": return "Move Down";
            case "RIGHT": return "Move Right";
            default: return bindId;
        }
    }

    private void createUI() {
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();
        mainTable.pad(30);

        // Title
        Label titleLabel = new Label("SETTINGS", skin);
        titleLabel.setFontScale(2f);
        titleLabel.setColor(0.9f, 0.3f, 0.3f, 1f);

        // Music Selection
        Table musicTable = new Table();
        Label musicLabel = new Label("Music Theme:  ", skin);
        musicSelect = new SelectBox<>(skin);
        musicSelect.setItems("Menu 1", "Menu 2");
        musicSelect.setSelected(controller.getSelectedMusic());
        musicSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.setSelectedMusic(musicSelect.getSelected());
                SoundManager.getInstance().play("select", 0.5f);
            }
        });
        musicTable.add(musicLabel).width(150).left().padRight(40);
        musicTable.add(musicSelect).width(200).left();
        musicLabel.setColor(0.9f, 0.3f, 0.3f, 1f);
        musicTable.setColor(0.9f, 0.3f, 0.3f, 1f);
        // Volume Slider
        Table volumeTable = new Table();
        Label volumeLabel = new Label("Music Volume:", skin);
        volumeSlider = new Slider(0f, 1f, 0.05f, false, skin);
        volumeSlider.setValue(controller.getMusicVolume());
        Label volumeValueLabel = new Label(String.format("%.0f%%", controller.getMusicVolume() * 100), skin);
        volumeLabel.setColor(0.9f, 0.3f, 0.3f, 1f);
        volumeTable.setColor(0.9f, 0.3f, 0.3f, 1f);
        volumeValueLabel.setColor(0.9f, 0.3f, 0.3f, 1f);
        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = volumeSlider.getValue();
                controller.setMusicVolume(value);
                volumeValueLabel.setText(String.format("%.0f%%", value * 100));
            }
        });

        volumeTable.add(volumeLabel).width(150).left().padRight(40);
        volumeTable.add(volumeSlider).width(150).left().padRight(10);
        volumeTable.add(volumeValueLabel).width(50).left();

        // Sound Effects Checkbox
        Table sfxTable = new Table();
        Label sfxLabel = new Label("Sound Effects:", skin);
        sfxCheckbox = new CheckBox("", skin);
        sfxCheckbox.setChecked(controller.isSfxEnabled());
        sfxCheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.setSfxEnabled(sfxCheckbox.isChecked());
                if (sfxCheckbox.isChecked()) {
                    SoundManager.getInstance().play("select", 0.5f);
                }
            }
        });
        sfxTable.add(sfxLabel).width(150).left().padRight(40);
        sfxTable.add(sfxCheckbox).left();
        sfxTable.setColor(0.9f, 0.3f, 0.3f, 1f);
        sfxLabel.setColor(0.9f, 0.3f, 0.3f, 1f);
        // Auto Reload Checkbox (under SFX)
        Table autoReloadTable = new Table();
        Label autoReloadLabel = new Label("Auto Reload:", skin);
        CheckBox autoReloadCheckbox = new CheckBox("", skin);
        autoReloadCheckbox.setChecked(controller.isAutoReloadEnabled());
        autoReloadCheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.setAutoReloadEnabled(autoReloadCheckbox.isChecked());
                SoundManager.getInstance().play("select", 0.5f);
            }
        });
        autoReloadTable.add(autoReloadLabel).width(150).left().padRight(30);
        autoReloadTable.add(autoReloadCheckbox).left();
        autoReloadLabel.setColor(0.9f, 0.3f, 0.3f, 1f);
        autoReloadTable.setColor(0.9f, 0.3f, 0.3f, 1f);
        // Key Bindings Label
        Label keysLabel = new Label("KEY BINDINGS", skin);
        keysLabel.setFontScale(1.5f);
        keysLabel.setColor(0.9f, 0.6f, 0.3f, 1f);

        // D-Pad Table
        dPadTable = new Table();
        dPadTable.setName("dPadTable");

        TextButton upButton = createKeyBindButton("UP", Input.Keys.toString(controller.getKeyBinding("UP")));
        TextButton leftButton = createKeyBindButton("LEFT", Input.Keys.toString(controller.getKeyBinding("LEFT")));
        TextButton downButton = createKeyBindButton("DOWN", Input.Keys.toString(controller.getKeyBinding("DOWN")));
        TextButton rightButton = createKeyBindButton("RIGHT", Input.Keys.toString(controller.getKeyBinding("RIGHT")));

        dPadTable.add().width(120).height(100);
        dPadTable.add(upButton).width(120).height(100).pad(5);
        dPadTable.add().width(120).height(100).row();
        upButton.setColor(0.9f, 0.3f, 0.3f, 1f);
        dPadTable.add(leftButton).width(120).height(100).pad(5);
        dPadTable.add().width(120).height(100);
        dPadTable.add(rightButton).width(120).height(100).pad(5).row();
        leftButton.setColor(0.9f, 0.3f, 0.3f, 1f);
        rightButton.setColor(0.9f, 0.3f, 0.3f, 1f);
        dPadTable.add().width(120).height(100);
        dPadTable.add(downButton).width(120).height(100).pad(5);
        dPadTable.add().width(120).height(100);
        downButton.setColor(0.9f, 0.3f, 0.3f, 1f);
        // Auto Aim
        autoaimButton = createKeyBindButton("AUTOAIM", Input.Keys.toString(controller.getKeyBinding("AUTOAIM")));
        autoaimButton.setColor(0.9f, 0.3f, 0.3f, 1f);
        autoaimTable = new Table();
        autoaimTable.add(new Label("Autoaim:", skin)).padRight(5).bottom();
        autoaimTable.add(autoaimButton).width(120).height(100).bottom();

        // Grayscale "Z" Button (next to D-pad)

        TextButton grayscaleKeyButton = createRedButton("Z");
        grayscaleKeyButton.setColor(0.9f, 0.3f, 0.3f, 1f);
        grayscaleKeyButton.setDisabled(true);

        Table dPadAndGrayscale = new Table();
        dPadAndGrayscale.add(dPadTable).padRight(40);
        dPadAndGrayscale.add(autoaimTable).padRight(40);
        dPadAndGrayscale.add(new Label("Grayscale:", skin)).padRight(5).bottom();
        dPadAndGrayscale.add(grayscaleKeyButton).width(120).height(100).bottom();

        // Waiting for input label
        waitingForInput = new Label("Press any key...", skin);
        waitingForInput.setVisible(false);

        // Back Button
        TextButton backButton = createRedButton("BACK");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.saveSettings();
                controller.backToMainMenu();
                dispose();
            }
        });
        backButton.setColor(0.9f, 0.3f, 0.3f, 1f);
        // Layout
        mainTable.add(titleLabel).padBottom(30).row();
        mainTable.add(musicTable).width(350).padBottom(20).row();
        mainTable.add(volumeTable).width(350).padBottom(20).row();
        mainTable.add(sfxTable).width(350).padBottom(10).row();
        mainTable.add(autoReloadTable).width(350).padBottom(40).row();
        mainTable.add(keysLabel).padBottom(20).row();
        mainTable.add(dPadAndGrayscale).padBottom(25).row();
        mainTable.add(waitingForInput).padBottom(30).row();
        mainTable.add(backButton).width(200).height(100).padBottom(20).row();

        stage.addActor(mainTable);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Handle key rebinding
        if (currentRebinding != null) {
            for (int i = 0; i < 256; i++) {
                if (Gdx.input.isKeyJustPressed(i)) {
                    controller.setKeyBinding(currentRebinding, i);
                    updateKeyBindings();
                    SoundManager.getInstance().play("select", 0.5f);
                    currentRebinding = null;
                    waitingForInput.setVisible(false);
                    break;
                }
            }
        }

        // Render background
        AssetManager.getInstance().renderBackground(delta);

        stage.act(delta);
        stage.draw();
    }

    private void updateKeyBindings() {
        dPadTable.clear();

        TextButton upButton = createKeyBindButton("UP", Input.Keys.toString(controller.getKeyBinding("UP")));
        TextButton leftButton = createKeyBindButton("LEFT", Input.Keys.toString(controller.getKeyBinding("LEFT")));
        TextButton downButton = createKeyBindButton("DOWN", Input.Keys.toString(controller.getKeyBinding("DOWN")));
        TextButton rightButton = createKeyBindButton("RIGHT", Input.Keys.toString(controller.getKeyBinding("RIGHT")));
        autoaimButton.setText(Input.Keys.toString(controller.getKeyBinding("AUTOAIM")));
        // Recreate the D-pad layout
        dPadTable.add().width(120).height(50);
        dPadTable.add(upButton).width(120).height(100).pad(5);
        dPadTable.add().width(120).height(100).row();
        upButton.setColor(0.9f, 0.3f, 0.3f, 1f);
        dPadTable.add(leftButton).width(120).height(100).pad(5);
        dPadTable.add().width(120).height(100);
        dPadTable.add(rightButton).width(120).height(100).pad(5).row();
        leftButton.setColor(0.9f, 0.3f, 0.3f, 1f);
        rightButton.setColor(0.9f, 0.3f, 0.3f, 1f);
        dPadTable.add().width(120).height(100);
        dPadTable.add(downButton).width(120).height(100).pad(5);
        dPadTable.add().width(120).height(100);
        downButton.setColor(0.9f, 0.3f, 0.3f, 1f);
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
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
}
