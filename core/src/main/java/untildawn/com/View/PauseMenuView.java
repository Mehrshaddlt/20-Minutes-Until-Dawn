package untildawn.com.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import untildawn.com.Controller.PauseMenuController;
import untildawn.com.Model.AssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import untildawn.com.Main;
import untildawn.com.Controller.EndScreenController;
import untildawn.com.Controller.GameController;
import untildawn.com.Model.SaveGameManager;

import java.util.Map;

public class PauseMenuView {
    private final PauseMenuController controller;
    private final Stage stage;
    private final Skin skin;
    private final Color menuColor = new Color(1.0f, 0.45f, 0.45f, 1f);

    private Table mainTable;
    private Table contentTable;
    private boolean showingCheats = false;
    private boolean showingAbilities = false;

    private final String[] cheatCodes = {
        "L - Level Up",
        "K - Skip 1 Minute",
        "H - Add Heart",
        "N - Go to Boss Fight",
        "P - Immediately exit game"
    };

    public PauseMenuView(PauseMenuController controller) {
        this.controller = controller;
        this.stage = new Stage(new ScreenViewport());
        this.skin = AssetManager.getInstance().getSkin();

        createUI();
    }

    private void createUI() {
        mainTable = new Table();
        mainTable.setFillParent(true);

        Window window = new Window("", skin);
        window.setColor(menuColor);
        window.setMovable(false);
        window.pad(20);

        contentTable = new Table();
        contentTable.center();

        createMainMenu();

        window.add(contentTable).expand().fill();
        mainTable.add(window).width(600).height(900).center();

        stage.addActor(mainTable);
    }

    private TextButton createRedButton(String text) {
        TextButton button = new TextButton(text, skin);
        button.setColor(menuColor);
        return button;
    }

    private void createMainMenu() {
        contentTable.clear();
        contentTable.center();

        // Game title
        Label titleLabel = new Label("PAUSE MENU", skin);
        titleLabel.setFontScale(1.5f);
        titleLabel.setColor(menuColor);
        titleLabel.setAlignment(Align.center);
        contentTable.add(titleLabel).padBottom(40).center().row();

        // Resume button
        TextButton resumeButton = createRedButton("RESUME");
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleMenuSelection(0);
            }
        });
        contentTable.add(resumeButton).width(300).height(90).padBottom(25).center().row();

        // Grayscale button directly under Resume
        TextButton grayscaleButton = createRedButton("TOGGLE GRAYSCALE");
        grayscaleButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.toggleGrayscale();
            }
        });
        contentTable.add(grayscaleButton).width(550).height(100).padBottom(25).center().row();

        // Other menu options
        String[] menuOptions = {"CHEAT", "ABILITIES", "EXIT GAME"};
        for (int i = 0; i < menuOptions.length; i++) {
            final int index = i + 1; // Offset by 1 since Resume is index 0
            TextButton button = createRedButton(menuOptions[i]);
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    handleMenuSelection(index);
                }
            });
            contentTable.add(button).width(300).height(90).padBottom(25).center().row();
        }
    }

    private void createCheatCodesMenu() {
        contentTable.clear();
        contentTable.center();

        Label titleLabel = new Label("CHEATS", skin);
        titleLabel.setFontScale(1.5f);
        titleLabel.setColor(menuColor);
        titleLabel.setAlignment(Align.center);
        contentTable.add(titleLabel).padBottom(30).center().row();

        Table cheatTable = new Table();
        for (String cheat : cheatCodes) {
            Label cheatLabel = new Label(cheat, skin);
            cheatLabel.setFontScale(1.1f);
            cheatLabel.setColor(Color.WHITE);
            cheatTable.add(cheatLabel).left().padBottom(15).row();
        }
        contentTable.add(cheatTable).padBottom(30).center().row();

        TextButton backButton = createRedButton("BACK");
        contentTable.add(backButton).width(180).height(90).center();

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showingCheats = false;
                showingAbilities = false;
                createMainMenu();
            }
        });
    }

    private void createAbilitiesMenu() {
        contentTable.clear();
        contentTable.center();

        // Title
        Label titleLabel = new Label("ABILITIES", skin);
        titleLabel.setFontScale(1.5f);
        titleLabel.setColor(menuColor);
        contentTable.add(titleLabel).padBottom(30).center().row();

        // Get player abilities
        Map<String, Integer> playerAbilities = controller.getPlayerAbilityNames();

        // Debug output to console
        System.out.println("Player abilities: " + playerAbilities);

        if (playerAbilities == null || playerAbilities.isEmpty()) {
            Label noAbilitiesLabel = new Label("No abilities acquired", skin);
            noAbilitiesLabel.setFontScale(1.1f);
            noAbilitiesLabel.setColor(Color.WHITE);
            contentTable.add(noAbilitiesLabel).padBottom(30).center().row();
        }
        else {
            // Just create a simple list of all abilities
            Table abilitiesTable = new Table();

            for (Map.Entry<String, Integer> entry : playerAbilities.entrySet()) {
                String abilityName = entry.getKey();
                int count = entry.getValue();

                String displayText = abilityName;
                if (count > 1) {
                    displayText += " x" + count;
                }

                Label abilityLabel = new Label(displayText, skin);
                abilityLabel.setFontScale(1.1f);

                // Use different colors based on ability type for visual distinction
                if (abilityName.equals("Damager") || abilityName.equals("Speedy")) {
                    abilityLabel.setColor(Color.YELLOW); // Temporary abilities
                } else {
                    abilityLabel.setColor(Color.WHITE); // Permanent abilities
                }

                abilitiesTable.add(abilityLabel).left().padBottom(15).row();
            }

            contentTable.add(abilitiesTable).padBottom(30).center().row();
        }

        // Back button
        TextButton backButton = createRedButton("BACK");
        contentTable.add(backButton).width(180).height(90).center();

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showingCheats = false;
                showingAbilities = false;
                createMainMenu();
            }
        });
    }
    public void showExitConfirmation() {
        final Dialog dialog = new Dialog("Exit Game", skin) {
            @Override
            protected void result(Object object) {
                boolean saveGame = (boolean) object;
                if (saveGame) {
                    // User chose "Yes" - exit without showing end screen
                    SaveGameManager.saveLastGame(controller.getGameController());
                    controller.getGame().setScreen(new PreMenuView(controller.getGame()));
                } else {
                    // User chose "No" - show lose screen
                    GameView gameView = (GameView) controller.getGame().getScreen();

                    // Get screenshot
                    Texture backgroundTexture = gameView.getScreenshot();

                    // Get remaining time
                    int remainingTime = controller.getGameController().getTotalGameTimeInSeconds();

                    EndScreenController endScreenController = new EndScreenController(
                        controller.getGame(),
                        controller.getGameController(),
                        false, // game lost
                        remainingTime,
                        controller.getPlayer().getUser()
                    );

                    controller.getGame().setScreen(new EndScreenView(
                        controller.getGame(),
                        endScreenController,
                        remainingTime,
                        backgroundTexture
                    ));
                }
            }
        };

        dialog.text("Do you want to save your progress?");
        dialog.button("Yes", true);
        dialog.button("No", false);
        dialog.show(stage);
    }
    public void render() {
        if (!controller.isPaused()) {
            return;
        }

        // Update the stage
        stage.act(Gdx.graphics.getDeltaTime());

        // Draw semi-transparent overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        stage.getBatch().begin();
        stage.getBatch().setColor(0, 0, 0, 0.7f);
        stage.getBatch().draw(skin.getRegion("white"), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.getBatch().end();

        // Handle UI navigation based on current state
        if (showingCheats && contentTable.getChildren().size == 0) {
            createCheatCodesMenu();
        } else if (showingAbilities && contentTable.getChildren().size == 0) {
            createAbilitiesMenu();
        } else if (!showingCheats && !showingAbilities && contentTable.getChildren().size == 0) {
            createMainMenu();
        }

        // Draw the stage
        stage.draw();
    }

    private void handleMenuSelection(int option) {
        switch (option) {
            case 0: // Resume
                controller.resume();
                break;
            case 1: // Cheat Codes
                showingCheats = true;
                createCheatCodesMenu();
                break;
            case 2: // Abilities
                showingAbilities = true;
                createAbilitiesMenu();
                break;
            case 3: // Exit Game
                showExitConfirmation(); // Changed from controller.exitGame()
                break;
        }
    }

    public boolean handleClick(float x, float y) {
        if (!controller.isPaused()) {
            return false;
        }

        // Convert screen coordinates to stage coordinates
        stage.setKeyboardFocus(mainTable);

        // Let the stage handle the input
        return stage.hit(x, y, false) != null;
    }

    public Stage getStage() {
        return stage;
    }

    public void dispose() {
        stage.dispose();
    }
}
