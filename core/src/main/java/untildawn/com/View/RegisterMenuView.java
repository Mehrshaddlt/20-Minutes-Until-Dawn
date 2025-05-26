package untildawn.com.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import untildawn.com.Controller.RegisterMenuController;
import untildawn.com.Main;
import untildawn.com.Model.AssetManager;
import untildawn.com.Model.SoundManager;

import java.util.regex.Pattern;

public class RegisterMenuView implements Screen {
    private final Main game;
    private final Stage stage;
    private final RegisterMenuController controller;
    private Skin skin;

    // Registration state
    private boolean showSecurityQuestion = false;
    private String username = "";
    private String password = "";

    // UI components
    private Table mainTable;
    private Table registrationTable;
    private Table securityTable;
    private Label errorLabel;

    // Animation properties
    private float fadeInTime = 0;
    private final float FADE_DURATION = 1.0f;
    private boolean isFadingIn = true;
    private ShapeRenderer shapeRenderer;

    public RegisterMenuView(Main game) {
        this.game = game;
        this.controller = new RegisterMenuController(game);
        this.stage = new Stage(new ScreenViewport());
        this.shapeRenderer = new ShapeRenderer();
        this.skin = AssetManager.getInstance().getSkin();
        createRegistrationForm();
        createUI();
        Gdx.input.setInputProcessor(stage);

        // Start with fade effect
        stage.getRoot().getColor().a = 0;
    }

    private void createUI() {
        mainTable = new Table();
        mainTable.setFillParent(true);

        // Create registration form
        createRegistrationForm();

        // Create security question form (initially hidden)
        createSecurityQuestionForm();

        // Start with registration form
        mainTable.add(registrationTable);
        stage.addActor(mainTable);
    }

    private void createRegistrationForm() {
        registrationTable = new Table();

        // Title
        Label titleLabel = new Label("REGISTER", skin);
        titleLabel.setFontScale(1.5f);
        registrationTable.add(titleLabel).colspan(2).padBottom(30).row();

        // Username field
        registrationTable.add(new Label("Username:", skin)).align(Align.left).padRight(10);
        TextField usernameField = new TextField("", skin);
        usernameField.setMaxLength(15); // Limit characters to prevent overflow
        registrationTable.add(usernameField).width(250).padBottom(15).row();

        // Password field
        registrationTable.add(new Label("Password:", skin)).align(Align.left).padRight(10);
        TextField passwordField = new TextField("", skin);
        passwordField.setPasswordCharacter('*');
        passwordField.setPasswordMode(true);
        passwordField.setMaxLength(20); // Limit characters
        registrationTable.add(passwordField).width(250).padBottom(15).row();

        // Error label
        errorLabel = new Label("", skin);
        errorLabel.setColor(1, 0.3f, 0.3f, 1);
        errorLabel.setWrap(true);
        errorLabel.setAlignment(Align.center);
        registrationTable.add(errorLabel).colspan(2).width(400).padBottom(20).row();

        // Register button
        TextButton registerButton = new TextButton("REGISTER", skin);
        registerButton.setColor(1.0f, 0.45f, 0.45f, 1f);
        registerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                username = usernameField.getText();
                password = passwordField.getText();

                if (validateRegistration()) {
                    // Switch to security question
                    showSecurityQuestion = true;
                    mainTable.clear();
                    mainTable.add(securityTable);
                }
            }
        });
        registrationTable.add(registerButton).colspan(2).width(300).padBottom(15).row();

        // Guest button
        TextButton guestButton = new TextButton("PLAY AS GUEST", skin);
        guestButton.setColor(1.0f, 0.45f, 0.45f, 1f); // Make sure it has the same red color
        guestButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.playAsGuest();
            }
        });
        registrationTable.add(guestButton).colspan(2).width(450).padBottom(15).row();

        // Back button
        TextButton backButton = new TextButton("BACK", skin);
        backButton.setColor(1.0f, 0.45f, 0.45f, 1f);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.goBack();
            }
        });
        registrationTable.add(backButton).colspan(2).width(250).row();
    }

    private void createSecurityQuestionForm() {
        securityTable = new Table();

        // Title
        Label titleLabel = new Label("SECURITY QUESTION", skin);
        titleLabel.setFontScale(1.5f);
        securityTable.add(titleLabel).padBottom(30).row();

        // Instructions
        Label infoLabel = new Label("Choose a security question:", skin);
        securityTable.add(infoLabel).padBottom(20).row();

        // Security questions
        final String[] questions = {
            "What was your first pet's name?",
            "What city were you born in?",
            "What was the first school you attended?"
        };

        final SelectBox<String> questionSelect = new SelectBox<>(skin);
        questionSelect.setItems(questions);
        securityTable.add(questionSelect).width(1200).padBottom(15).row();

        // Answer field
        securityTable.add(new Label("Your answer:", skin)).align(Align.left).padBottom(5).row();
        final TextField answerField = new TextField("", skin);
        answerField.setMaxLength(30); // Limit characters
        securityTable.add(answerField).width(350).padBottom(20).row(); // Wider field

        // Complete button
        TextButton completeButton = new TextButton("COMPLETE REGISTRATION", skin);
        completeButton.setColor(1.0f, 0.45f, 0.45f, 1f); // Matching red color
        completeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                String securityQuestion = questionSelect.getSelected();
                String securityAnswer = answerField.getText();

                // Try to register and check if it was successful
                boolean success = controller.register(username, password, securityQuestion, securityAnswer);

                if (!success) {
                    // Create and show error message from controller
                    Label errorLabel = new Label(controller.getLastError(), skin);
                    errorLabel.setColor(1, 0.3f, 0.3f, 1);
                    errorLabel.setAlignment(Align.center);
                    securityTable.add(errorLabel).width(400).padTop(10).row();
                }
            }
        });
        securityTable.add(completeButton).width(800).padBottom(15).row();
    }

    private boolean validateRegistration() {
        // First check if username exists in the system
        if (!controller.isUsernameAvailable(username)) {
            errorLabel.setText(controller.getLastError());
            return false;
        }

        // Then validate the rest of the registration data
        boolean isValid = controller.validateRegistrationData(username, password);
        if (!isValid) {
            errorLabel.setText(controller.getLastError());
        }

        return isValid;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render background
        AssetManager.getInstance().renderBackground(delta);

        // Handle fade-in animation
        if (isFadingIn) {
            fadeInTime += delta;
            float progress = Math.min(fadeInTime / FADE_DURATION, 1.0f);

            // Fade IN stage UI elements
            stage.getRoot().getColor().a = progress;

            // Fade OUT overlay
            float overlayAlpha = 1.0f - progress;

            if (overlayAlpha > 0) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(0, 0, 0, overlayAlpha);
                shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                shapeRenderer.end();

                Gdx.gl.glDisable(GL20.GL_BLEND);
            } else {
                isFadingIn = false;
            }
        }

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
        shapeRenderer.dispose();
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
