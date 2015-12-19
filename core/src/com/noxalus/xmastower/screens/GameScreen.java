package com.noxalus.xmastower.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noxalus.xmastower.Assets;
import com.noxalus.xmastower.Config;
import com.noxalus.xmastower.State;
import com.noxalus.xmastower.XmasTower;
import com.noxalus.xmastower.entities.Gift;
import com.noxalus.xmastower.entities.SpriteActor;
import com.noxalus.xmastower.inputs.GameInputProcessor;

public class GameScreen extends ApplicationAdapter implements Screen {

    private static final String TAG = "GameScreen";

    private XmasTower _game;

    GameInputProcessor _gameInputProcessor;

    Vector2 _cameraTarget;
    BitmapFont _font;
    boolean _needToAddNewGift;
    boolean _cameraIsMoving;
    public float _score;
    private float _bestScore;
    private Sprite _bestScoreLine;
    Sound _currentPlayedSound;
    public boolean GameWillReset;
    public boolean GameIsFinished;
    private Preferences _preferences;
    private SpriteActor _groundSpriteActor;
    private float _currentMaxHeight;
    private float _cameraSavedYPosition;
    public float CameraSpeedY;
    private boolean _zooming;

    InputMultiplexer _inputMultiplexer;

    // UI
    Stage _uiStage;
    Label _bestScoreLabel;
    Label _scoreLabel;
    Label _distanceToBestScoreLabel;
    Boolean _showDistanceToBestScore = false;
    private Button _playAgainButton;
    private Button _zoomOutButton;
    ImageButton _backButton;

    // Physics
    Body _groundBody;

    public GameScreen(XmasTower game) {
        _game = game;
        _font = new BitmapFont();

        _gameInputProcessor = new GameInputProcessor(_game, this);

        _groundSpriteActor = new SpriteActor(new Sprite(Assets.groundTexture));

        _preferences = Gdx.app.getPreferences("xmas-tower");
        _bestScore = _preferences.getFloat("highscore", 0f);

        _bestScoreLine = new Sprite(Assets.barleySugarTexture);
        _bestScoreLine.setScale(Config.RESOLUTION_SCALE_RATIO.x, Config.RESOLUTION_SCALE_RATIO.y);

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
        if (_score > _bestScore) {
            _preferences.putFloat("highscore", _score);
            _preferences.flush();
            _bestScore = _score;
        }

        clean();

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

        Assets.gameMusicIntro.play();

        addGift();

        _scoreLabel.setText(Config.SCORE_LABEL_PLACEHOLDER);

        if (_bestScore > 0f) {
            _bestScoreLine.setPosition(0,
                    (_bestScore * Config.HEIGHT_UNIT_FACTOR) +
                    _groundSpriteActor.sprite.getHeight());
            _bestScoreLabel.setPosition(0, (_bestScore * Config.HEIGHT_UNIT_FACTOR) +
                    ((_bestScoreLine.getHeight()) * 2f) +
                    (_bestScoreLabel.getPrefHeight() * _bestScoreLabel.getFontScaleY()) / 2f);
            _game.Stage.addActor(_bestScoreLabel);
        }

        _playAgainButton.setVisible(false);
        _backButton.setVisible(false);
        _zoomOutButton.setVisible(false);
    }

    private void initializeUI() {
        _uiStage = new Stage(new FitViewport(720, 1280));

        Label.LabelStyle normalLabelStyle = new Label.LabelStyle(Assets.normalFont, Color.WHITE);
        Label.LabelStyle mediumLabelStyle = new Label.LabelStyle(Assets.mediumFont, Color.WHITE);

        _scoreLabel = new Label(Config.SCORE_LABEL_PLACEHOLDER, Assets.menuSkin);
        _scoreLabel.setStyle(mediumLabelStyle);
        _scoreLabel.setAlignment(Align.center);
        _scoreLabel.setWidth(Gdx.graphics.getWidth());
        _scoreLabel.setScale(Config.RESOLUTION_SCALE_RATIO.x, Config.RESOLUTION_SCALE_RATIO.y);
        _scoreLabel.setPosition(
                0,
                (Gdx.graphics.getHeight() -
                        (_scoreLabel.getHeight()) / 2f) -
                        (Gdx.graphics.getHeight() / 30f)
        );

        _bestScoreLabel = new Label("Best", Assets.menuSkin);
        _bestScoreLabel.setStyle(normalLabelStyle);
        _bestScoreLabel.setFontScale(Config.RESOLUTION_SCALE_RATIO.x, Config.RESOLUTION_SCALE_RATIO.y);

        SpriteDrawable playButtonUpSprite = new SpriteDrawable(new Sprite(Assets.playButtonUp));
        SpriteDrawable playButtonDownSprite = new SpriteDrawable(new Sprite(Assets.playButtonDown));

        _playAgainButton = new ImageButton(playButtonUpSprite, playButtonDownSprite);

        SpriteDrawable zoomOutButtonUpSprite = new SpriteDrawable(new Sprite(Assets.zoomOutButtonUp));
        SpriteDrawable zoomOutButtonDownSprite = new SpriteDrawable(new Sprite(Assets.zoomOutButtonDown));

        _zoomOutButton = new ImageButton(zoomOutButtonUpSprite, zoomOutButtonDownSprite);

        SpriteDrawable backButtonUpSprite = new SpriteDrawable(new Sprite(Assets.backButtonUp));
        SpriteDrawable backButtonDownSprite = new SpriteDrawable(new Sprite(Assets.backButtonDown));

        _backButton = new ImageButton(backButtonUpSprite, backButtonDownSprite);

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

        _uiStage.setDebugAll(true);
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
                    backToMenu();
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
                        _bestScoreLabel.setVisible(true);

                    } else {
                        float ratio = (_currentMaxHeight + Gdx.graphics.getHeight()) / Gdx.graphics.getHeight();

                        if (ratio > 1f) {
                            _cameraSavedYPosition = _game.Camera.position.y;
                            _game.Camera.zoom = ratio;
                            _game.Camera.position.y = _currentMaxHeight / 2f;
                            _zooming = true;

                            // Hide some interface elements
                            _scoreLabel.setVisible(false);
                            _bestScoreLabel.setVisible(false);
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
    }

    private void backToMenu() {
        clean();
        _game.setScreen(_game.MenuScreen);
    }

    public void update(float delta) {
        if (!GameWillReset) {
            for (int i = 0; i < _game.Gifts.size(); i++) {
                Gift currentGift = _game.Gifts.get(i);

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

                    Vector3 screenCoordinates = _game.Camera.project(
                        new Vector3(currentGift.getX(), currentGift.getY(), 0.f)
                    );
                    float limitThreshold = Gdx.graphics.getWidth() / 1.5f;

                    if (screenCoordinates.y > limitThreshold)
                        translateCamera(new Vector2(0f, Gdx.graphics.getWidth() / 2f));

                    currentGift.getBox().getHeight();


                    if (_currentMaxHeight < currentGift.getY())
                    {
                        _currentMaxHeight = currentGift.getY();
                        Gdx.app.log(TAG, "Current max height: " + _currentMaxHeight);
                    }

                    float currentScore = (
                        (currentGift.getY()) -
                        _groundSpriteActor.sprite.getHeight()
                    ) / Config.HEIGHT_UNIT_FACTOR;

                    if (currentScore > _score) {
                        _score = ((Math.round(currentScore * 10f)) / 10f); // Round to 1 decimal after comma
                        _scoreLabel.setText(Float.toString(_score) + " cm");
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

        if (!_zooming && _bestScore > 0f) {
            _game.SpriteBatch.begin();
            _bestScoreLine.draw(_game.SpriteBatch, 1f);
            _game.SpriteBatch.end();
        }

        _uiStage.draw();

        _game.SpriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render(float delta) {
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
