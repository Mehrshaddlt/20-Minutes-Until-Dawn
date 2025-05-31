package untildawn.com.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import untildawn.com.Controller.MainMenuController;
import untildawn.com.Main;
import untildawn.com.Model.AssetManager;
import untildawn.com.Model.SoundManager;
import untildawn.com.Model.User;

public class TalentMenuView implements Screen {
    private final Main game;
    private final Stage stage;
    private final MainMenuController controller;
    private final User currentUser;
    private Skin skin;
    private final Preferences prefs;

    public TalentMenuView(Main game, User currentUser) {
        this.game = game;
        this.currentUser = currentUser;
        this.controller = new MainMenuController(game);
        this.controller.setUser(currentUser);
        this.stage = new Stage(new ScreenViewport());
        this.skin = AssetManager.getInstance().getSkin();
        this.prefs = Gdx.app.getPreferences("untildawn_settings");

        createUI();
        Gdx.input.setInputProcessor(stage);
    }

    private TextButton createRedButton(String text) {
        TextButton button = new TextButton(text, skin);
        button.setColor(1.0f, 0.45f, 0.45f, 1f);
        return button;
    }

    private void createUI() {
        Table mainTable = new Table();
        mainTable.setFillParent(true);

        Label titleLabel = new Label("GAME INFORMATION", skin);
        titleLabel.setFontScale(2f);
        titleLabel.setColor(1.0f, 0.45f, 0.45f, 1f);

        // Heroes Information
        Table heroesTable = new Table();
        heroesTable.background(skin.getDrawable("window"));

        Label heroesTitle = new Label("HEROES", skin);
        heroesTitle.setFontScale(1.5f);
        heroesTitle.setColor(1.0f, 0.45f, 0.45f, 1f);

        heroesTable.add(heroesTitle).colspan(3).padBottom(10).row();
        addHeroStats(heroesTable, "Shana", 4, 4);
        addHeroStats(heroesTable, "Diamond", 7, 1);
        addHeroStats(heroesTable, "Scarlet", 3, 5);
        addHeroStats(heroesTable, "Lilith", 5, 3);
        addHeroStats(heroesTable, "Dasher", 3, 10);
        heroesTable.setColor(1.0f, 0.45f, 0.45f, 1f);
        // Controls Information
        Table controlsTable = new Table();
        controlsTable.background(skin.getDrawable("window"));

        Label controlsTitle = new Label("CONTROLS", skin);
        controlsTitle.setFontScale(1.5f);
        controlsTitle.setColor(1.0f, 0.45f, 0.45f, 1f);

        controlsTable.add(controlsTitle).colspan(2).padBottom(10).row();

        // Get current bindings from preferences
        int upKey = prefs.getInteger("key_UP", Input.Keys.W);
        int downKey = prefs.getInteger("key_DOWN", Input.Keys.S);
        int leftKey = prefs.getInteger("key_LEFT", Input.Keys.A);
        int rightKey = prefs.getInteger("key_RIGHT", Input.Keys.D);

        addControlInfo(controlsTable, "Move Up", Input.Keys.toString(upKey));
        addControlInfo(controlsTable, "Move Down", Input.Keys.toString(downKey));
        addControlInfo(controlsTable, "Move Left", Input.Keys.toString(leftKey));
        addControlInfo(controlsTable, "Move Right", Input.Keys.toString(rightKey));
        addControlInfo(controlsTable, "Shoot", "Left Click");
        controlsTable.setColor(1.0f, 0.45f, 0.45f, 1f);
        // Cheat Codes
        Table cheatsTable = new Table();
        cheatsTable.background(skin.getDrawable("window"));

        Label cheatsTitle = new Label("CHEAT CODES", skin);
        cheatsTitle.setFontScale(1.5f);
        cheatsTitle.setColor(1.0f, 0.45f, 0.45f, 1f);

        cheatsTable.add(cheatsTitle).colspan(2).padBottom(10).row();
        addCheatInfo(cheatsTable, "P", "Leave game immediately");
        addCheatInfo(cheatsTable, "L", "Level up");
        addCheatInfo(cheatsTable, "K", "Pass 1 minute of time");
        addCheatInfo(cheatsTable, "H", "Heal fully");
        cheatsTable.setColor(1.0f, 0.45f, 0.45f, 1f);
        // Abilities
        Table abilitiesTable = new Table();
        abilitiesTable.background(skin.getDrawable("window"));

        Label abilitiesTitle = new Label("ABILITIES", skin);
        abilitiesTitle.setFontScale(1.5f);
        abilitiesTitle.setColor(1.0f, 0.45f, 0.45f, 1f);

        abilitiesTable.add(abilitiesTitle).colspan(2).padBottom(10).row();
        addAbilityInfo(abilitiesTable, "Vitality", "Adds 1 extra Heart");
        addAbilityInfo(abilitiesTable, "Damager", "Increases damage by 25% for 10 seconds");
        addAbilityInfo(abilitiesTable, "Procrease", "Adds another projectile");
        addAbilityInfo(abilitiesTable, "Amocrease", "Adds 5 maximum ammo");
        addAbilityInfo(abilitiesTable, "Speedy", "Doubles speed for 10 seconds");
        abilitiesTable.setColor(1.0f, 0.45f, 0.45f, 1f);
        // Back button
        TextButton backButton = createRedButton("BACK");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                game.setScreen(new MainMenuView(game, currentUser));
            }
        });

        // Layout
        mainTable.add(titleLabel).colspan(2).padBottom(20).row();

        Table leftColumn = new Table();
        leftColumn.add(heroesTable).expand().fill().padBottom(20).row();
        leftColumn.add(controlsTable).expand().fill().row();

        Table rightColumn = new Table();
        rightColumn.add(cheatsTable).expand().fill().padBottom(20).row();
        rightColumn.add(abilitiesTable).expand().fill().row();

        mainTable.add(leftColumn).expand().fill().padRight(10);
        mainTable.add(rightColumn).expand().fill().padLeft(10).row();

        mainTable.add(backButton).width(300).height(100).colspan(2).padTop(20);

        stage.addActor(mainTable);
    }

    private void addHeroStats(Table table, String name, int hp, int speed) {
        Label nameLabel = new Label(name, skin);
        Label hpLabel = new Label("HP: " + hp, skin);
        Label speedLabel = new Label("Speed: " + speed, skin);

        table.add(nameLabel).padRight(10);
        table.add(hpLabel).padRight(10);
        table.add(speedLabel).row();
    }

    private void addControlInfo(Table table, String action, String key) {
        Label actionLabel = new Label(action + ":", skin);
        Label keyLabel = new Label(key, skin);

        table.add(actionLabel).padRight(10).left();
        table.add(keyLabel).left().row();
    }

    private void addCheatInfo(Table table, String key, String effect) {
        Label keyLabel = new Label(key, skin);
        Label effectLabel = new Label(effect, skin);

        table.add(keyLabel).padRight(10).left();
        table.add(effectLabel).left().row();
    }

    private void addAbilityInfo(Table table, String name, String description) {
        Label nameLabel = new Label(name + ":", skin);
        Label descLabel = new Label(description, skin);

        table.add(nameLabel).padRight(10).left();
        table.add(descLabel).left().row();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
