package com.noxalus.xmastower.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noxalus.xmastower.Assets;
import com.noxalus.xmastower.Config;
import com.noxalus.xmastower.State;
import com.noxalus.xmastower.XmasTower;
import com.noxalus.xmastower.entities.Gift;
import com.noxalus.xmastower.entities.SpriteActor;
import com.noxalus.xmastower.inputs.GameInputProcessor;

import java.util.ArrayList;

public class GameScreen extends ApplicationAdapter implements Screen {

    private static final String TAG = "GameScreen";

    private XmasTower _game;

    GameInputProcessor _gameInputProcessor;

    private ArrayList<Gift> _gifts = new ArrayList<Gift>();
    Vector2 _cameraTarget;
    BitmapFont _font;
    boolean _needToAddNewGift;
    boolean _cameraIsMoving;
    public float _score;
    private float _bestScore;
    private Sprite _bestScoreLine;
    Sound _currentPlayedSound;
    public boolean GameWillReset;
    private Preferences _preferences;
    private SpriteActor _groundSpriteActor;

    // UI
    OrthographicCamera _uiCamera;
    Viewport _uiViewport;
    Stage _uiStage;
    Label _bestScoreLabel;
    Label _scoreLabel;
    Label _distanceToBestScoreLabel;
    Boolean _showDistanceToBestScore = false;

    // Physics
    Body _groundBody;

    public GameScreen(XmasTower game) {
        _game = game;
        _font = new BitmapFont();

        _gameInputProcessor = new GameInputProcessor(_game);

        _groundSpriteActor = new SpriteActor(new Sprite(Assets.groundTexture));
        float minGroundScale = 1.f;
        float maxGroundScale = (Gdx.graphics.getWidth() / _groundSpriteActor.sprite.getWidth()) / 1.5f;
        float groundScale = MathUtils.random(minGroundScale, maxGroundScale);

        float groundRealWidth = _groundSpriteActor.sprite.getWidth() * groundScale;
        float xPosition = (Gdx.graphics.getWidth() - groundRealWidth) / 2f;

        Gdx.app.log(TAG, "Ground scale: " + groundScale);
        _groundSpriteActor.setScale(groundScale, 1f);
        _groundSpriteActor.setPosition(xPosition, 0);

        _preferences = Gdx.app.getPreferences("xmas-tower");
        _bestScore = _preferences.getFloat("highscore", 0f);

        _bestScoreLine = new Sprite(Assets.whitePixel, Gdx.graphics.getWidth(), 10);
        _bestScoreLine.setColor(Color.RED);

        // UI
        _uiCamera = new OrthographicCamera();
        _uiViewport = new ScreenViewport(_uiCamera);
        _uiCamera.position.set(0, 0, 0);
        _uiStage = new Stage(_uiViewport);

        Label.LabelStyle normalLabelStyle = new Label.LabelStyle(Assets.normalFont, Color.WHITE);
        Label.LabelStyle mediumLabelStyle = new Label.LabelStyle(Assets.mediumFont, Color.WHITE);

        _scoreLabel = new Label(Config.SCORE_LABEL_PLACEHOLDER, Assets.menuSkin);
        _scoreLabel.setStyle(mediumLabelStyle);
        _scoreLabel.setAlignment(Align.center);
        _scoreLabel.setWidth(Gdx.graphics.getWidth());
        _scoreLabel.setPosition(0, (Gdx.graphics.getHeight() - _scoreLabel.getHeight() / 2f) - (Gdx.graphics.getHeight() / 30f));

        _bestScoreLabel = new Label("Best", Assets.menuSkin);
        _bestScoreLabel.setStyle(normalLabelStyle);

        _uiStage.addActor(_scoreLabel);
    }

    @Override
    public void show() {
        _game.OnMenu = false;
        Gdx.input.setInputProcessor(_gameInputProcessor);
        initializePhysics();

        reset();
    }

    public void reset() {
        _game.Camera.position.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0);
//        _game.Camera.zoom = 5.f;
        _cameraTarget = new Vector2(_game.Camera.position.x, _game.Camera.position.y);
        _needToAddNewGift = false;
        GameWillReset = false;
        _game.Stage.clear();
        _game.MouseJoint = null;
        _game.HitBody = null;

        _game.Stage.addActor(_groundSpriteActor);

        if (_score > _bestScore) {
            _preferences.putFloat("highscore", _score);
            _preferences.flush();
            _bestScore = _score;
        }

        _score = 0;

        _game.DestroyMouseJoint = false;
        _game.pausePhysics(false);
        _currentPlayedSound = null;

        Assets.gameMusicLoop.stop();
        Assets.gameMusicIntro.stop();
        Assets.gameMusicIntro.play();

        for (Gift g : _gifts)
        {
            _game.World.destroyBody(g.getBody());
        }

        _gifts.clear();

        addGift();

        _scoreLabel.setText(Config.SCORE_LABEL_PLACEHOLDER);

        if (_bestScore > 0f) {
            _bestScoreLine.setPosition(0, _bestScore + _groundSpriteActor.sprite.getHeight());
            _bestScoreLabel.setPosition(0, _bestScore + (_bestScoreLine.getHeight() * 2f) + _bestScoreLabel.getPrefHeight() / 2f);
            _game.Stage.addActor(_bestScoreLabel);
        }
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

        Gift gift = new Gift(new Vector2(
                Gdx.graphics.getWidth() / 2f,
                _game.Camera.position.y + (Gdx.graphics.getHeight() / 2f) - 100f
            )
        );

        gift.initializePhysics(_game.World);
        _game.Stage.addActor(gift);
        gift.setZIndex(0);
        _groundSpriteActor.setZIndex(0);
        _gifts.add(gift);
    }

    public void gameFinished() {
        _game.pausePhysics(true);
        translateCamera(new Vector2(0, -(_game.Camera.position.y - Gdx.graphics.getHeight() / 2f)));
        GameWillReset = true;
    }

    public void update(float delta) {
        if (!GameWillReset) {
            for (int i = 0; i < _gifts.size(); i++) {
                Gift currentGift = _gifts.get(i);

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

                    float currentScore = (
                            (currentGift.getY() * currentGift.getScaleY()) -
                            _groundSpriteActor.sprite.getHeight()
                    ) / 50f;

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
        Vector3 position = _game.Camera.position;

        float cameraInterpolationThreshold = 10.f;
        if (Math.abs(position.x - _cameraTarget.x) > cameraInterpolationThreshold ||
                Math.abs(position.y - _cameraTarget.y) > cameraInterpolationThreshold) {
            float lerp = 0.05f;

            position.x = position.x + (_cameraTarget.x - position.x) * lerp;
            position.y = position.y + (_cameraTarget.y - position.y) * lerp;

            _game.Camera.position.set(position);
        }
        else if (_cameraIsMoving) {
            Gdx.app.log(TAG, "CAMERA STOP MOVING");
            _cameraIsMoving = false;

            if (_needToAddNewGift) {
                _needToAddNewGift = false;
                addGift();
            }

            if (GameWillReset) {
                GameWillReset = false;
                reset();
            }
        }
    }

    public void translateCamera(Vector2 target) {
        _cameraIsMoving = true;
        _cameraTarget = new Vector2(_game.Camera.position.x, _game.Camera.position.y).add(target);
    }

    public void draw(float delta) {
        _game.SpriteBatch.setProjectionMatrix(_game.Camera.combined);

        if (_bestScore > 0f) {
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
