package com.noxalus.xmastower.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.noxalus.xmastower.Assets;
import com.noxalus.xmastower.Config;
import com.noxalus.xmastower.State;
import com.noxalus.xmastower.XmasTower;
import com.noxalus.xmastower.entities.Gift;
import com.noxalus.xmastower.entities.SpriteActor;
import com.noxalus.xmastower.gameservices.ActionResolver;
import com.noxalus.xmastower.inputs.GameInputProcessor;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;

public class GameScreen extends ApplicationAdapter implements Screen {

    private static final String TAG = "GameScreen";

    private XmasTower _game;

    GameInputProcessor _gameInputProcessor;

    Vector2 _cameraTarget;
    BitmapFont _font;
    boolean _needToAddNewGift;
    boolean _cameraIsMoving;
    public float _score;
    private float _highScore;
    private float _highScoreHeight; // in pixel
    private Sprite _highScoreLine;
    Sound _currentPlayedSound;
    public boolean GameWillReset;
    public boolean GameIsFinished;
    private Preferences _preferences;
    private SpriteActor _groundSpriteActor;
    private float _currentMaxHeight;
    private float _cameraSavedYPosition;
    public float CameraSpeedY;
    private boolean _zooming;
    private boolean _isSickAchievement = false;
    private int _gameNumber;

    InputMultiplexer _inputMultiplexer;

    // UI
    private Stage _uiStage;
    private Label _scoreLabel;
    private Label _distanceToBestScoreLabel;
    private Boolean _showDistanceToBestScore = false;
    private Button _playAgainButton;
    private Button _zoomOutButton;
    private ImageButton _backButton;

    SpriteDrawable _shareButtonUpSprite;
    SpriteDrawable _shareButtonDownSprite;

    SpriteDrawable _backButtonUpSprite;
    SpriteDrawable _backButtonDownSprite;

    // Physics
    Body _groundBody;

    public GameScreen(XmasTower game) {
        _game = game;
        _font = new BitmapFont();

        _gameInputProcessor = new GameInputProcessor(_game, this);

        _groundSpriteActor = new SpriteActor(new Sprite(Assets.groundTexture));

        _preferences = Gdx.app.getPreferences("xmas-tower");
        _highScore = _preferences.getFloat("highscore", 0f);
        _highScoreHeight = _preferences.getFloat("highscoreHeight", 0f);

        _gameNumber = _preferences.getInteger("gameNumber", 0);

        _highScoreLine = new Sprite(Assets.barleySugarTexture);
        _highScoreLine.setScale(Config.RESOLUTION_SCALE_RATIO.x, Config.RESOLUTION_SCALE_RATIO.y);

        // UI
        initializeUI();

        _inputMultiplexer = new InputMultiplexer(_uiStage, _gameInputProcessor);
    }

    @Override
    public void show() {
        _game.OnMenu = false;
        Gdx.input.setInputProcessor(_inputMultiplexer);
        reset();
    }

    private void clean() {
        Assets.gameMusicLoop.stop();
        Assets.gameMusicIntro.stop();

        CameraSpeedY = 0f;
        GameWillReset = false;
        GameIsFinished = false;
        _game.Stage.clear();
        _game.Camera.zoom = 1f;
        _game.Camera.position.x = Gdx.graphics.getWidth() / 2f;
        _game.Camera.position.y = Gdx.graphics.getHeight() / 2f;
        _game.MouseJoint = null;
        _game.HitBody = null;
        _game.DestroyMouseJoint = false;
        _game.pausePhysics(false);

        _cameraTarget = new Vector2(_game.Camera.position.x, _game.Camera.position.y);
        _needToAddNewGift = false;
        _currentMaxHeight = 0f;
        _zooming = false;
        _cameraIsMoving = false;
        _score = 0;
        _currentPlayedSound = null;

        if (_groundBody != null) {
            _game.World.destroyBody(_groundBody);
            _groundBody = null;
        }

        for (Gift g : _game.Gifts)
            _game.World.destroyBody(g.getBody());
        _game.Gifts.clear();
    }

    private void reset() {
        clean();

        _gameNumber++;
        _preferences.putInteger("gameNumber", _gameNumber);
        _preferences.flush();

        // Generate a random size ground
        float minGroundScale = 1.f;
        float maxGroundScale = (Gdx.graphics.getWidth() / _groundSpriteActor.sprite.getWidth()) / 1.5f;
        float groundScale = MathUtils.random(minGroundScale, maxGroundScale);

        float groundRealWidth = _groundSpriteActor.sprite.getWidth() * groundScale;
        float xPosition = (Gdx.graphics.getWidth() - groundRealWidth) / 2f;

        _groundSpriteActor.setScale(groundScale, 1f);
        _groundSpriteActor.setPosition(xPosition, 0);

        _game.Stage.addActor(_groundSpriteActor);

        initializePhysics();

        if (_game.SoundsEnabled)
            Assets.gameMusicIntro.play();

        addGift();

        _scoreLabel.setText(Config.SCORE_LABEL_PLACEHOLDER);

        if (_highScoreHeight > 0f) {
            _highScoreLine.setPosition(0, _highScoreHeight);
        }

        _playAgainButton.setVisible(false);
        _backButton.setVisible(false);
        _zoomOutButton.setVisible(false);

        _backButton.getStyle().imageUp = _backButtonUpSprite;
        _backButton.getStyle().imageDown = _backButtonDownSprite;
    }

    private void initializeUI() {
        _uiStage = new Stage(new FitViewport(Config.IDEAL_RESOLUTION.x, Config.IDEAL_RESOLUTION.y));

        if (Config.ENABLE_UI_DEBUG)
            _uiStage.setDebugAll(true);

        Label.LabelStyle normalLabelStyle = new Label.LabelStyle(Assets.normalFont, Color.WHITE);
        Label.LabelStyle mediumLabelStyle = new Label.LabelStyle(Assets.mediumFont, Color.WHITE);

        _scoreLabel = new Label(Config.SCORE_LABEL_PLACEHOLDER, Assets.menuSkin);
        _scoreLabel.setStyle(mediumLabelStyle);

        SpriteDrawable playButtonUpSprite = new SpriteDrawable(new Sprite(Assets.playButtonUp));
        SpriteDrawable playButtonDownSprite = new SpriteDrawable(new Sprite(Assets.playButtonDown));

        _playAgainButton = new ImageButton(playButtonUpSprite, playButtonDownSprite);

        SpriteDrawable zoomOutButtonUpSprite = new SpriteDrawable(new Sprite(Assets.zoomOutButtonUp));
        SpriteDrawable zoomOutButtonDownSprite = new SpriteDrawable(new Sprite(Assets.zoomOutButtonDown));

        _zoomOutButton = new ImageButton(zoomOutButtonUpSprite, zoomOutButtonDownSprite);

        _shareButtonUpSprite = new SpriteDrawable(new Sprite(Assets.shareButtonUp));
        _shareButtonDownSprite = new SpriteDrawable(new Sprite(Assets.shareButtonDown));

        _backButtonUpSprite = new SpriteDrawable(new Sprite(Assets.backButtonUp));
        _backButtonDownSprite = new SpriteDrawable(new Sprite(Assets.backButtonDown));

        _backButton = new ImageButton(_backButtonUpSprite, _backButtonDownSprite);

        Table scoreTable = new Table();
        scoreTable.setFillParent(true);
        scoreTable.setSize(_uiStage.getWidth(), _uiStage.getHeight());
        scoreTable.align(Align.center | Align.top);
        scoreTable.add(_scoreLabel);

        Table buttonsTable = new Table();
        buttonsTable.setFillParent(true);
        buttonsTable.align(Align.center | Align.bottom);
        buttonsTable.padBottom(50);
        buttonsTable.add(_backButton).pad(150, 0, 0, 30);
        buttonsTable.add(_playAgainButton);
        buttonsTable.add(_zoomOutButton).pad(150, 30, 0, 0);

        _uiStage.addActor(scoreTable);
        _uiStage.addActor(buttonsTable);

        // Buttons click events
        _playAgainButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (GameIsFinished) {
                    reset();
                }
            }
        });

        _backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (GameIsFinished) {

                    if (!_zooming)
                    {
                        backToMenu();
                    }
                    else
                    {
                        byte[] pixelData = ScreenUtils.getFrameBufferPixels(true);
                        Pixmap pixmap = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Pixmap.Format.RGBA8888);
                        ByteBuffer pixels = pixmap.getPixels();
                        pixels.clear();
                        pixels.put(pixelData);
                        pixels.position(0);

                        Date date = new Date(TimeUtils.millis());
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
                        String formatedDate = formatter.format(date);
                        FileHandle screenshot = Gdx.files.external("Screenshots/Xmas-Tower/Screenshot_" + formatedDate + ".png");
                        PixmapIO.writePNG(screenshot, pixmap);

                        String screenshotAbsolutePath = Gdx.files.getExternalStoragePath() + "/" + screenshot.path();

                        // Social share
                        _game.SharableActivity.shareImage(screenshotAbsolutePath, _score);
                    }
                }
            }
        });

        _zoomOutButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (GameIsFinished) {
                    if (_zooming) {
                        _zooming = false;
                        _game.Camera.zoom = 1f;
                        _game.Camera.position.y = _cameraSavedYPosition;

                        // Show again some interface elements
                        _scoreLabel.setVisible(true);
                        _playAgainButton.setVisible(true);

                        _backButton.getStyle().imageUp = _backButtonUpSprite;
                        _backButton.getStyle().imageDown = _backButtonDownSprite;

                    } else {
                        float ratio = (_currentMaxHeight + Gdx.graphics.getHeight()) / Gdx.graphics.getHeight();

                        if (ratio > 1f) {
                            _cameraSavedYPosition = _game.Camera.position.y;
                            _game.Camera.zoom = ratio;
                            _game.Camera.position.y = _currentMaxHeight / 2f;
                            _zooming = true;

                            // Hide some interface elements
                            _scoreLabel.setVisible(false);
                            _playAgainButton.setVisible(false);

                            _backButton.getStyle().imageUp = _shareButtonUpSprite;
                            _backButton.getStyle().imageDown = _shareButtonDownSprite;
                        }
                    }
                }
            }
        });
    }

    private void initializePhysics() {
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.type = BodyDef.BodyType.StaticBody;

        groundBodyDef.position.set(
            (_groundSpriteActor.getX() + (_groundSpriteActor.sprite.getWidth() / 2f) *
                    _groundSpriteActor.getScaleX()) / Config.PIXELS_TO_METERS,
            (_groundSpriteActor.getY() + (_groundSpriteActor.sprite.getHeight() / 2f) *
                    _groundSpriteActor.getScaleY()) / Config.PIXELS_TO_METERS
        );

        _groundBody = _game.World.createBody(groundBodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
            (((_groundSpriteActor.sprite.getWidth() / 2f)) * _groundSpriteActor.getScaleX()) / Config.PIXELS_TO_METERS,
            ((_groundSpriteActor.sprite.getHeight() / 2f) * _groundSpriteActor.getScaleY()) / Config.PIXELS_TO_METERS,
            new Vector2(0, 0),
            0f
        );

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        _groundBody.createFixture(fixtureDef);
        shape.dispose();
    }

    public void addGift() {

        if (_cameraIsMoving) {
            _needToAddNewGift = true;
            return;
        }

        Vector2 giftPosition = new Vector2(
            Gdx.graphics.getWidth() / 2f,
            _game.Camera.position.y +
            (Gdx.graphics.getHeight() / 2f) -
            (Gdx.graphics.getHeight() / 10f)
        );

        _game.addGift(giftPosition);
    }

    public void gameFinished() {
        _game.pausePhysics(true);
        translateCamera(new Vector2(0, -(_game.Camera.position.y - Gdx.graphics.getHeight() / 2f)));
        GameWillReset = true;

        // Save the highscore
        if (_score > _highScore) {
            _highScore = _score;
            _highScoreHeight = _currentMaxHeight;
            _highScoreLine.setPosition(0, _highScoreHeight);

            // Store data in user preferences
            _preferences.putFloat("highscore", _score);
            _preferences.putFloat("highscoreHeight", _highScoreHeight);
            _preferences.flush();
        }

        // Submit the score to leaderboard
        if (_game.ActionResolver.getSignedInGPGS())
            _game.ActionResolver.submitScoreGPGS((int)(_score * 10)); // Need to be an integer

        // Achivements unlocked?
        if (_game.ActionResolver.getSignedInGPGS())
        {
            if (_score >= 100 && _score < 150)
                _game.ActionResolver.unlockAchievementGPGS(ActionResolver.Achievement.ACHIEVEMENT_1_M);
            else if (_score >= 150 && _score < 200)
                _game.ActionResolver.unlockAchievementGPGS(ActionResolver.Achievement.ACHIEVEMENT_1_5_M);
            else if (_score >= 200 && _score < 250)
                _game.ActionResolver.unlockAchievementGPGS(ActionResolver.Achievement.ACHIEVEMENT_2_M);
            else if (_score >= 250 && _score < 300)
                _game.ActionResolver.unlockAchievementGPGS(ActionResolver.Achievement.ACHIEVEMENT_2_5_M);
            else if (_score >= 300 && _score < 350)
                _game.ActionResolver.unlockAchievementGPGS(ActionResolver.Achievement.ACHIEVEMENT_3_M);
            else if (_score >= 350 && _score < 400)
                _game.ActionResolver.unlockAchievementGPGS(ActionResolver.Achievement.ACHIEVEMENT_3_5_M);
            else if (_score >= 400 && _score < 450)
                _game.ActionResolver.unlockAchievementGPGS(ActionResolver.Achievement.ACHIEVEMENT_4_M);
            else if (_score >= 450 && _score < 500)
                _game.ActionResolver.unlockAchievementGPGS(ActionResolver.Achievement.ACHIEVEMENT_4_5_M);
            else if (_score >= 500 && _score < 750)
                _game.ActionResolver.unlockAchievementGPGS(ActionResolver.Achievement.ACHIEVEMENT_5_M);
            else if (_score >= 750 && _score < 1000)
                _game.ActionResolver.unlockAchievementGPGS(ActionResolver.Achievement.ACHIEVEMENT_7_5_M);
            else if (_score >= 1000 && _score < 150)
                _game.ActionResolver.unlockAchievementGPGS(ActionResolver.Achievement.ACHIEVEMENT_10_M);
        }
    }

    private void backToMenu() {
        clean();
        _game.setScreen(_game.MenuScreen);
    }

    public void update(float delta) {
        if (!GameWillReset) {
            for (int i = 0; i < _game.Gifts.size(); i++) {
                Gift currentGift = _game.Gifts.get(i);

                if (!_isSickAchievement && currentGift.isSick() && _game.ActionResolver.getSignedInGPGS()) {
                    _isSickAchievement = true;
                    _game.ActionResolver.unlockAchievementGPGS(ActionResolver.Achievement.ACHIEVEMENT_IM_FEELING_DIZZY);
                }

                // Outside of the scene?
                if (currentGift.getX() < -(currentGift.getBox().sprite.getWidth() * 2f) ||
                    currentGift.getX() > (Gdx.graphics.getWidth() + currentGift.getBox().sprite.getWidth()) ||
                    currentGift.getY() < 0) {
                    gameFinished();
                } else if (!currentGift.isPlaced() && !currentGift.isMovable() &&
                        currentGift.getBody().getLinearVelocity().x < Config.LINEAR_VELOCITY_THRESHOLD &&
                        currentGift.getBody().getLinearVelocity().x > -Config.LINEAR_VELOCITY_THRESHOLD &&
                        currentGift.getBody().getLinearVelocity().y < Config.LINEAR_VELOCITY_THRESHOLD &&
                        currentGift.getBody().getLinearVelocity().y > -Config.LINEAR_VELOCITY_THRESHOLD) {
                    currentGift.isPlaced(true);

                    currentGift.switchState(State.SLEEPING);

                    float currentHeight = currentGift.getHighestPosition();
                    if (currentHeight > _currentMaxHeight) {
                        _currentMaxHeight = currentHeight;

                        float currentScore = _currentMaxHeight;
                        currentScore -= _groundSpriteActor.sprite.getHeight() * _groundSpriteActor.sprite.getScaleY(); // We remove the ground
                        currentScore /= Config.RESOLUTION_SCALE_RATIO.y; // Resolution uniformisation
                        currentScore /= Config.HEIGHT_UNIT_FACTOR; // Arbitrary factor to get centimeters

                        if (currentScore > _score) {
                            _score = ((Math.round(currentScore * 10f)) / 10f); // Round to 1 decimal after comma
                            _scoreLabel.setText(Float.toString(_score) + " cm");
                        }
                    }

                    float screenHeight = _currentMaxHeight - _game.Camera.position.y;

                    if (screenHeight > -Gdx.graphics.getHeight() / 4f) {
                        float distance = screenHeight + Gdx.graphics.getHeight() / 3.5f;

                        translateCamera(new Vector2(0f, distance));
                    }

                    addGift();
                }

                currentGift.update(delta);
            }
        }

        updateCamera(Gdx.graphics.getDeltaTime());

        _uiStage.act(delta);
    }

    private void updateCamera(float delta) {

        if (_zooming) {
            CameraSpeedY = 0;
            return;
        }

        if (GameIsFinished) {
            // Camera inertia
            CameraSpeedY *= Config.CAMERA_INERTIA;

            if (CameraSpeedY < 0 && _game.Camera.position.y <= Gdx.graphics.getHeight() / 2f) {
                CameraSpeedY = 0;
                _game.Camera.position.set(
                        _game.Camera.position.x,
                        Gdx.graphics.getHeight() / 2f,
                        _game.Camera.position.z
                );
            } else
                _game.Camera.position.y += CameraSpeedY;

            return;
        }

        Vector3 position = _game.Camera.position;

        if (Math.abs(position.x - _cameraTarget.x) > Config.CAMERA_INTERPOLATION_THRESHOLD ||
            Math.abs(position.y - _cameraTarget.y) > Config.CAMERA_INTERPOLATION_THRESHOLD) {

            position.x = position.x + (_cameraTarget.x - position.x) * Config.CAMERA_TRANSLATION_INTERPOLATION;
            position.y = position.y + (_cameraTarget.y - position.y) * Config.CAMERA_TRANSLATION_INTERPOLATION;

            _game.Camera.position.set(position);
        }
        else if (_cameraIsMoving) {
            _cameraIsMoving = false;

            if (_needToAddNewGift) {
                _needToAddNewGift = false;
                addGift();
            }

            if (GameWillReset) {
                GameWillReset = false;
                GameIsFinished = true;
                _playAgainButton.setVisible(true);
                _backButton.setVisible(true);
                _zoomOutButton.setVisible(true);
            }
        }
    }

    public void translateCamera(Vector2 target) {
        _cameraIsMoving = true;
        _cameraTarget = new Vector2(_game.Camera.position.x, _game.Camera.position.y).add(target);
    }

    public void draw(float delta) {
        _game.SpriteBatch.setProjectionMatrix(_game.Camera.combined);

        if (!_zooming && _highScoreLine.getY() > 0f) {
            _game.SpriteBatch.begin();
            _highScoreLine.draw(_game.SpriteBatch, 1f);
            Assets.mediumFont.draw(_game.SpriteBatch, "Best", 10,
                (_highScoreHeight + 10) +
                (_highScoreLine.getHeight() * _highScoreLine.getScaleY()) +
                Assets.mediumFont.getData().capHeight
            );
            _game.SpriteBatch.end();
        }

        _game.drawGifts();

        _uiStage.draw();

        _game.SpriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render(float delta)
    {
        update(delta);
        draw(delta);
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
    }
}
