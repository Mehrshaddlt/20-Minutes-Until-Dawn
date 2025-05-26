package untildawn.com.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.Color;

import untildawn.com.Main;
import untildawn.com.Model.AssetManager;
import untildawn.com.Model.User;
import untildawn.com.Controller.LeaderboardMenuController;

import java.util.List;

public class LeaderboardMenuView implements Screen {
    private Stage stage;
    private LeaderboardMenuController controller;
    private Skin skin;
    private Table mainTable;
    private Table leaderboardTable;
    private Label sortModeLabel;
    private User currentUser;

    // Colors for highlighting
    private static final Color GOLD_COLOR = new Color(1, 0.84f, 0, 1);
    private static final Color SILVER_COLOR = new Color(0.75f, 0.75f, 0.75f, 1);
    private static final Color BRONZE_COLOR = new Color(0.8f, 0.5f, 0.2f, 1);
    private static final Color CURRENT_USER_COLOR = new Color(0.2f, 0.8f, 0.2f, 1);

    public LeaderboardMenuView(Main game, User currentUser) {
        this.currentUser = currentUser;
        this.stage = new Stage(new ScreenViewport());
        this.skin = AssetManager.getInstance().getSkin();

        // Create controller
        this.controller = new LeaderboardMenuController(game, currentUser);
        this.controller.setView(this);

        createUI();
        updateLeaderboard();

        Gdx.input.setInputProcessor(stage);
    }

    private void createUI() {
        stage.clear();

        mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        // Add title
        Label titleLabel = new Label("LEADERBOARD", skin, "title");
        titleLabel.setFontScale(1.5f);
        mainTable.add(titleLabel).colspan(2).pad(30).row();

        // Add sort controls
        Table sortTable = new Table();
        Label sortByLabel = new Label("Sort by:", skin);
        sortByLabel.setFontScale(1.2f);
        sortModeLabel = new Label(controller.getSortModeString(), skin);
        sortModeLabel.setFontScale(1.2f);

        TextButton prevSortButton = new TextButton("<", skin);
        prevSortButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.setSortMode((controller.getSortMode() + 2) % 3); // Previous mode
                sortModeLabel.setText(controller.getSortModeString());
            }
        });

        TextButton nextSortButton = new TextButton(">", skin);
        nextSortButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.setSortMode((controller.getSortMode() + 1) % 3); // Next mode
                sortModeLabel.setText(controller.getSortModeString());
            }
        });

        sortTable.add(sortByLabel).padRight(15);
        sortTable.add(prevSortButton).size(50, 50).padRight(15);
        sortTable.add(sortModeLabel).width(220).align(Align.center);
        sortTable.add(nextSortButton).size(50, 50).padLeft(15);

        mainTable.add(sortTable).colspan(2).pad(20).row();

        // Create header for leaderboard
        Table headerTable = new Table();
        // Try to use the button drawable if it exists, otherwise don't set a background
        try {
            headerTable.setBackground(skin.getDrawable("button"));
        } catch (Exception e) {
            System.err.println("Warning: Could not find button drawable in skin");
        }

        Label rankHeader = new Label("RANK", skin);
        rankHeader.setFontScale(1.2f);
        headerTable.add(rankHeader).width(80).pad(10);

        Label usernameHeader = new Label("USERNAME", skin);
        usernameHeader.setFontScale(1.2f);
        headerTable.add(usernameHeader).width(260).pad(10);

        Label scoreHeader = new Label("SCORE", skin);
        scoreHeader.setFontScale(1.2f);
        headerTable.add(scoreHeader).width(140).pad(10);

        Label killsHeader = new Label("KILLS", skin);
        killsHeader.setFontScale(1.2f);
        headerTable.add(killsHeader).width(120).pad(10);

        mainTable.add(headerTable).colspan(2).width(900).pad(10).row();

        // Create scrollable leaderboard table
        leaderboardTable = new Table();
        ScrollPane scrollPane = new ScrollPane(leaderboardTable, skin);
        scrollPane.setScrollingDisabled(true, false);

        mainTable.add(scrollPane).colspan(2).width(900).height(450).pad(10).row();

        TextButton backButton = new TextButton("BACK", skin);
        backButton.getLabel().setFontScale(1.5f);  // Larger text
        backButton.setColor(0.2f, 0.6f, 0.9f, 1f); // Blue tint

        // Add a small effect when hovering
        backButton.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                backButton.getLabel().setFontScale(1.6f);
                backButton.setColor(0.3f, 0.7f, 1f, 1f);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                backButton.getLabel().setFontScale(1.5f);
                backButton.setColor(0.2f, 0.6f, 0.9f, 1f);
            }
        });

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.goToMainMenu();
            }
        });

        mainTable.add(backButton).colspan(2).pad(30).size(320, 110).row();
    }

    public void updateLeaderboard() {
        leaderboardTable.clear();
        List<User> topUsers = controller.getTopUsers(10);

        for (int i = 0; i < topUsers.size(); i++) {
            User user = topUsers.get(i);

            Table rowTable = new Table();

            // Make entire rows for top 3 visually distinct
            if (i == 0) {
                // Gold background for #1
                rowTable.setBackground(skin.newDrawable("white", 1f, 0.84f, 0f, 0.3f));
            } else if (i == 1) {
                // Silver background for #2
                rowTable.setBackground(skin.newDrawable("white", 0.75f, 0.75f, 0.75f, 0.3f));
            } else if (i == 2) {
                // Bronze background for #3
                rowTable.setBackground(skin.newDrawable("white", 0.8f, 0.5f, 0.2f, 0.3f));
            } else if (i % 2 == 0) {
                // Alternating rows for the rest
                rowTable.setBackground(skin.newDrawable("white", 0.2f, 0.2f, 0.2f, 0.5f));
            }

            // Rank column with special styling for top 3
            Label rankLabel = new Label("#" + (i + 1), skin);
            Label usernameLabel = new Label(user.getUsername(), skin);
            Label scoreLabel = new Label(String.valueOf(user.getTotalScore()), skin);
            Label killsLabel = new Label(String.valueOf(user.getTotalKills()), skin);

            // Enhanced styling for top 3
            if (i == 0) {
                rankLabel.setColor(GOLD_COLOR);
                rankLabel.setFontScale(1.4f);
                usernameLabel.setFontScale(1.3f);
                scoreLabel.setFontScale(1.3f);
                killsLabel.setFontScale(1.3f);
            } else if (i == 1) {
                rankLabel.setColor(SILVER_COLOR);
                rankLabel.setFontScale(1.2f);
                usernameLabel.setFontScale(1.1f);
                scoreLabel.setFontScale(1.1f);
                killsLabel.setFontScale(1.1f);
            } else if (i == 2) {
                rankLabel.setColor(BRONZE_COLOR);
                rankLabel.setFontScale(1.1f);
                usernameLabel.setFontScale(1.05f);
                scoreLabel.setFontScale(1.05f);
                killsLabel.setFontScale(1.05f);
            }

            // Highlight current user
            if (currentUser != null && user.getUsername().equals(currentUser.getUsername())) {
                usernameLabel.setColor(CURRENT_USER_COLOR);
            }

            // Match these widths with the header widths exactly
            rowTable.add(rankLabel).width(80).pad(10).align(Align.center);
            rowTable.add(usernameLabel).width(260).pad(10).align(Align.left);
            rowTable.add(scoreLabel).width(140).pad(10).align(Align.center);
            rowTable.add(killsLabel).width(120).pad(10).align(Align.center);

            leaderboardTable.add(rowTable).fillX().expandX().row();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render background using AssetManager
        AssetManager.getInstance().renderBackground(delta);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
