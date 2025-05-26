package untildawn.com.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import untildawn.com.Controller.EndScreenController;
import untildawn.com.Main;
import untildawn.com.Model.User;

public class EndScreenView implements Screen {
    private final Main game;
    private final EndScreenController controller;
    private final Stage stage;
    private final BitmapFont titleFont;
    private final BitmapFont normalFont;

    // Animation properties
    private float animationTime = 0;
    private final float ANIMATION_DURATION = 1.5f;

    // UI components for animation
    private final Label titleLabel;
    private final Label killsLabel;
    private final Label timeLabel;
    private final TextButton returnButton;
    private final Image lineImage;
    private final HorizontalGroup scoreGroup; // Added as a field

    // Score calculation
    private final int timeInSeconds;
    private final int totalKills;
    private final int finalScore;
    private final Label usernameLabel;
    // Additional resources
    private final Texture backgroundTexture;
    private final Texture soulIcon;

    public EndScreenView(Main game, EndScreenController controller, int timeInSeconds, Texture backgroundTexture) {
        this.game = game;
        this.controller = controller;
        this.timeInSeconds = timeInSeconds;
        this.totalKills = controller.getKillCount();
        this.backgroundTexture = backgroundTexture;

        // Load soul icon
        this.soulIcon = new Texture("assets/T_SoulIcon.png");

        // Calculate score
        int totalGameTimeSeconds = controller.getTotalGameTimeInSeconds();
        if (controller.isGameWon()) {
            this.finalScore = totalGameTimeSeconds * totalKills;
        } else {
            int timeSurvived = totalGameTimeSeconds - timeInSeconds;
            this.finalScore = timeSurvived * totalKills;
        }

        User currentUser = controller.getCurrentUser();
        if (currentUser != null) {
            // Update user stats
            currentUser.setTotalGamesPlayed(currentUser.getTotalGamesPlayed() + 1);

            if (controller.isGameWon()) {
                currentUser.setGamesWon(currentUser.getGamesWon() + 1);
            }

            currentUser.setTotalKills(currentUser.getTotalKills() + totalKills);
            currentUser.setTotalScore(currentUser.getTotalScore() + finalScore);

            // Update high score if the current score is higher
            if (finalScore > currentUser.getHighScore()) {
                currentUser.setHighScore(finalScore);
            }
        }

        // Initialize stage
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Create fonts
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal("assets/ChevyRay - Express.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        // Title font
        parameter.size = 48;
        parameter.color = controller.isGameWon() ? Color.GOLD : Color.FIREBRICK;
        parameter.borderWidth = 2;
        parameter.borderColor = Color.BLACK;
        titleFont = generator.generateFont(parameter);

        // Normal font
        parameter.size = 24;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 1;
        normalFont = generator.generateFont(parameter);
        generator.dispose();

        // Create UI styles
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, titleFont.getColor());
        Label.LabelStyle normalStyle = new Label.LabelStyle(normalFont, normalFont.getColor());
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = normalFont;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.downFontColor = Color.LIGHT_GRAY;
        buttonStyle.overFontColor = Color.YELLOW;

        // Create UI components
        titleLabel = new Label(controller.isGameWon() ? "You Survived!" : "Game Over", titleStyle);
        titleLabel.setAlignment(Align.center);

        timeLabel = new Label("Total Time: " + (totalGameTimeSeconds / 60) + "m " +
            (totalGameTimeSeconds % 60) + "s", normalStyle);
        timeLabel.setAlignment(Align.center);

        killsLabel = new Label("Enemies Defeated: " + totalKills, normalStyle);
        killsLabel.setAlignment(Align.center);

        //Username label
        String username = currentUser != null ? currentUser.getUsername() : "Guest";
        usernameLabel = new Label("Player: " + username, normalStyle);
        usernameLabel.setAlignment(Align.center);

        // Create yellow line
        Pixmap pixmap = new Pixmap(400, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.YELLOW);
        pixmap.fill();
        Texture lineTexture = new Texture(pixmap);
        pixmap.dispose();
        lineImage = new Image(lineTexture);

        // Create score group with icon
        scoreGroup = new HorizontalGroup();
        scoreGroup.space(10);
        Label scoreLabel = new Label("Total Score: " + finalScore, normalStyle);
        scoreGroup.addActor(scoreLabel);
        scoreGroup.addActor(new Image(soulIcon));

        returnButton = new TextButton("Return to Main Menu", buttonStyle);
        returnButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.returnToMainMenu();
            }
        });

        // Position elements for animation
        titleLabel.setPosition(-800, Gdx.graphics.getHeight() * 0.7f);
        timeLabel.setPosition(Gdx.graphics.getWidth() + 400, Gdx.graphics.getHeight() * 0.6f);
        killsLabel.setPosition(-800, Gdx.graphics.getHeight() * 0.5f);
        usernameLabel.setPosition(Gdx.graphics.getWidth() + 400, Gdx.graphics.getHeight() * 0.45f); // Position for username
        lineImage.setPosition(Gdx.graphics.getWidth() / 2 - 200, Gdx.graphics.getHeight() * 0.4f - 20);
        lineImage.setColor(1, 1, 1, 0); // Start transparent
        scoreGroup.setPosition(Gdx.graphics.getWidth() + 400, Gdx.graphics.getHeight() * 0.3f);
        returnButton.setPosition(Gdx.graphics.getWidth() / 2 - 200, -100);
        // Add actors to stage
        stage.addActor(titleLabel);
        stage.addActor(timeLabel);
        stage.addActor(killsLabel);
        stage.addActor(usernameLabel);
        stage.addActor(lineImage);
        stage.addActor(scoreGroup);
        stage.addActor(returnButton);
    }

    @Override
    public void show() {
        // Reset animation
        animationTime = 0;
    }

    @Override
    public void render(float delta) {
        // Update animation
        animationTime = Math.min(animationTime + delta, ANIMATION_DURATION);
        float progress = animationTime / ANIMATION_DURATION;

        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw background (last frame of game)
        game.getBatch().begin();
        game.getBatch().draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.getBatch().end();

        // Calculate positions for animation
        float centerX = Gdx.graphics.getWidth() / 2 - 200;
        titleLabel.setPosition(
            calculateEaseOut(-800, centerX, progress),
            Gdx.graphics.getHeight() * 0.7f);

        timeLabel.setPosition(
            calculateEaseOut(Gdx.graphics.getWidth() + 400, centerX, progress),
            Gdx.graphics.getHeight() * 0.6f);

        killsLabel.setPosition(
            calculateEaseOut(-800, centerX, progress),
            Gdx.graphics.getHeight() * 0.5f);
        usernameLabel.setPosition(
            calculateEaseOut(Gdx.graphics.getWidth() + 400, centerX, progress),
            Gdx.graphics.getHeight() * 0.45f);
        // Fade in the line
        lineImage.setColor(1, 1, 1, calculateEaseOut(0, 1, progress));

        // Move in the score group
        scoreGroup.setPosition(
            calculateEaseOut(Gdx.graphics.getWidth() + 400, centerX, progress),
            Gdx.graphics.getHeight() * 0.3f);

        returnButton.setPosition(
            centerX,
            calculateEaseOut(-100, Gdx.graphics.getHeight() * 0.2f, progress));

        // Draw the stage
        stage.act(delta);
        stage.draw();
    }

    // Ease out function for smoother animation
    private float calculateEaseOut(float start, float end, float progress) {
        return start + (end - start) * (1 - (1 - progress) * (1 - progress));
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        titleFont.dispose();
        normalFont.dispose();
        soulIcon.dispose();
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }
}
