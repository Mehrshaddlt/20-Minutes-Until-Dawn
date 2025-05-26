package untildawn.com.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import untildawn.com.Controller.LoginMenuController;
import untildawn.com.Main;
import untildawn.com.Model.AssetManager;
import untildawn.com.Model.SoundManager;
import untildawn.com.Model.User;

public class LoginMenuView implements Screen {
    private final Main game;
    private final Stage stage;
    private final LoginMenuController controller;
    private Skin skin;

    // UI components
    private Table mainTable;
    private Table loginTable;
    private Table forgotPasswordTable;
    private Table securityQuestionTable;
    private Table newPasswordTable;
    private Label errorLabel;
    private String currentUsername = "";
    private Label questionLabel;

    // Animation properties
    private float fadeInTime = 0;
    private final float FADE_DURATION = 1.0f;
    private boolean isFadingIn = true;
    private ShapeRenderer shapeRenderer;

    public LoginMenuView(Main game) {
        this.game = game;
        this.controller = new LoginMenuController(game);
        this.stage = new Stage(new ScreenViewport());
        this.skin = AssetManager.getInstance().getSkin();
        this.shapeRenderer = new ShapeRenderer();

        createUI();
        Gdx.input.setInputProcessor(stage);

        // Start with fade effect
        stage.getRoot().getColor().a = 0;
    }

    private void createUI() {
        mainTable = new Table();
        mainTable.setFillParent(true);

        // Create login form
        createLoginForm();

        // Create forgot password form (initially hidden)
        createForgotPasswordForm();

        // Create security question form (initially hidden)
        createSecurityQuestionForm();

        // Create new password form (initially hidden)
        createNewPasswordForm();

        // Start with login form
        mainTable.add(loginTable);
        stage.addActor(mainTable);
    }

    private TextButton createRedButton(String text) {
        TextButton button = new TextButton(text, skin);
        button.setColor(1.0f, 0.45f, 0.45f, 1f);
        return button;
    }

    private void createLoginForm() {
        loginTable = new Table();

        // Title
        Label titleLabel = new Label("LOGIN", skin);
        titleLabel.setFontScale(1.5f);
        loginTable.add(titleLabel).colspan(2).padBottom(30).row();

        // Username field
        loginTable.add(new Label("Username:", skin)).align(Align.left).padRight(10);
        TextField usernameField = new TextField("", skin);
        usernameField.setMaxLength(15);
        loginTable.add(usernameField).width(250).padBottom(15).row();

        // Password field
        loginTable.add(new Label("Password:", skin)).align(Align.left).padRight(10);
        TextField passwordField = new TextField("", skin);
        passwordField.setPasswordCharacter('*');
        passwordField.setPasswordMode(true);
        passwordField.setMaxLength(20);
        loginTable.add(passwordField).width(250).padBottom(15).row();

        // Error label
        errorLabel = new Label("", skin);
        errorLabel.setColor(1, 0.3f, 0.3f, 1);
        errorLabel.setWrap(true);
        errorLabel.setAlignment(Align.center);
        loginTable.add(errorLabel).colspan(2).width(400).padBottom(20).row();

        // Login button
        TextButton loginButton = createRedButton("LOGIN");
        loginButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                String username = usernameField.getText();
                String password = passwordField.getText();

                boolean success = controller.login(username, password);
                if (!success) {
                    errorLabel.setText(controller.getLastError());
                }
            }
        });
        loginTable.add(loginButton).colspan(2).width(400).padBottom(15).row();

        // Forgot password button
        TextButton forgotButton = createRedButton("FORGOT PASSWORD");
        forgotButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                // Switch to forgot password form
                mainTable.clear();
                mainTable.add(forgotPasswordTable);
            }
        });
        loginTable.add(forgotButton).colspan(2).width(550).padBottom(15).row();

        // Register button
        TextButton registerButton = createRedButton("REGISTER");
        registerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.goToRegister();
            }
        });
        loginTable.add(registerButton).colspan(2).width(400).padBottom(15).row();

        // Back button
        TextButton backButton = createRedButton("BACK");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                controller.goBack();
            }
        });
        loginTable.add(backButton).colspan(2).width(400).row();
    }

    private void createForgotPasswordForm() {
        forgotPasswordTable = new Table();

        // Title
        Label titleLabel = new Label("FORGOT PASSWORD", skin);
        titleLabel.setFontScale(1.5f);
        forgotPasswordTable.add(titleLabel).padBottom(30).row();

        // Instructions
        Label infoLabel = new Label("Enter your username:", skin);
        forgotPasswordTable.add(infoLabel).padBottom(20).row();

        // Username field
        final TextField usernameField = new TextField("", skin);
        usernameField.setMaxLength(15);
        forgotPasswordTable.add(usernameField).width(400).padBottom(15).row();

        // Error label
        final Label fpErrorLabel = new Label("", skin);
        fpErrorLabel.setColor(1, 0.3f, 0.3f, 1);
        fpErrorLabel.setWrap(true);
        fpErrorLabel.setAlignment(Align.center);
        forgotPasswordTable.add(fpErrorLabel).width(400).padBottom(20).row();

        // Next button
        TextButton nextButton = createRedButton("NEXT");
        nextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                currentUsername = usernameField.getText();
                boolean success = controller.forgotPassword(currentUsername);

                if (success) {
                    // Update the security question before showing the form
                    User user = User.getUserByUsername(currentUsername);
                    questionLabel.setText(user.getSecurityQuestion());

                    mainTable.clear();
                    mainTable.add(securityQuestionTable);
                } else {
                    fpErrorLabel.setText(controller.getLastError());
                }
            }
        });
        forgotPasswordTable.add(nextButton).width(400).padBottom(15).row();

        // Back button
        TextButton backButton = createRedButton("BACK");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                // Return to login form
                mainTable.clear();
                mainTable.add(loginTable);
            }
        });
        forgotPasswordTable.add(backButton).width(400).row();
    }

    private void createSecurityQuestionForm() {
        securityQuestionTable = new Table();

        // Title
        Label titleLabel = new Label("SECURITY QUESTION", skin);
        titleLabel.setFontScale(1.5f);
        securityQuestionTable.add(titleLabel).padBottom(30).row();

        // Question display with placeholder text - we'll update it dynamically
        questionLabel = new Label("Security question will be shown here", skin);
        securityQuestionTable.add(questionLabel).width(400).padBottom(20).row();

        // Answer field
        securityQuestionTable.add(new Label("Your answer:", skin)).align(Align.left).padBottom(5).row();
        final TextField answerField = new TextField("", skin);
        answerField.setMaxLength(30);
        securityQuestionTable.add(answerField).width(400).padBottom(20).row();

        // Error label
        final Label sqErrorLabel = new Label("", skin);
        sqErrorLabel.setColor(1, 0.3f, 0.3f, 1);
        sqErrorLabel.setWrap(true);
        sqErrorLabel.setAlignment(Align.center);
        securityQuestionTable.add(sqErrorLabel).width(400).padBottom(20).row();

        // Verify button
        TextButton verifyButton = createRedButton("VERIFY");
        verifyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                String answer = answerField.getText();
                boolean success = controller.verifySecurityAnswer(currentUsername, answer);

                if (success) {
                    // Show new password form
                    mainTable.clear();
                    mainTable.add(newPasswordTable);
                } else {
                    sqErrorLabel.setText(controller.getLastError());
                }
            }
        });
        securityQuestionTable.add(verifyButton).width(400).padBottom(15).row();

        // Back button
        TextButton backButton = createRedButton("BACK");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                // Return to forgot password form
                mainTable.clear();
                mainTable.add(forgotPasswordTable);
            }
        });
        securityQuestionTable.add(backButton).width(200).row();
    }

    private void createNewPasswordForm() {
        newPasswordTable = new Table();

        // Title
        Label titleLabel = new Label("RESET PASSWORD", skin);
        titleLabel.setFontScale(1.5f);
        newPasswordTable.add(titleLabel).padBottom(30).row();

        // New password field
        newPasswordTable.add(new Label("New Password:", skin)).align(Align.left).padBottom(5).row();
        final TextField newPasswordField = new TextField("", skin);
        newPasswordField.setPasswordCharacter('*');
        newPasswordField.setPasswordMode(true);
        newPasswordField.setMaxLength(20);
        newPasswordTable.add(newPasswordField).width(400).padBottom(15).row();

        // Confirm password field
        newPasswordTable.add(new Label("Confirm Password:", skin)).align(Align.left).padBottom(5).row();
        final TextField confirmPasswordField = new TextField("", skin);
        confirmPasswordField.setPasswordCharacter('*');
        confirmPasswordField.setPasswordMode(true);
        confirmPasswordField.setMaxLength(20);
        newPasswordTable.add(confirmPasswordField).width(400).padBottom(15).row();

        // Error label
        final Label npErrorLabel = new Label("", skin);
        npErrorLabel.setColor(1, 0.3f, 0.3f, 1);
        npErrorLabel.setWrap(true);
        npErrorLabel.setAlignment(Align.center);
        newPasswordTable.add(npErrorLabel).width(400).padBottom(20).row();

        // Reset button
        TextButton resetButton = createRedButton("RESET PASSWORD");
        resetButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().play("select", 0.5f);
                String newPassword = newPasswordField.getText();
                String confirmPassword = confirmPasswordField.getText();

                if (!newPassword.equals(confirmPassword)) {
                    npErrorLabel.setText("Passwords do not match");
                    return;
                }

                boolean success = controller.resetPassword(currentUsername, newPassword);

                if (success) {
                    // Show success message and return to login
                    npErrorLabel.setColor(0.3f, 1f, 0.3f, 1f);
                    npErrorLabel.setText("Password reset successful!");
                    // Return to login after 2 seconds
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            mainTable.clear();
                            mainTable.add(loginTable);
                        }
                    }, 2);
                }
                else {
                    npErrorLabel.setColor(1f, 0.3f, 0.3f, 1f);
                    npErrorLabel.setText(controller.getLastError());
                }
            }
        });
        newPasswordTable.add(resetButton).width(500).padBottom(15).row();
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
