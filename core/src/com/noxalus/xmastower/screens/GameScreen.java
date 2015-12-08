package com.noxalus.xmastower.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noxalus.xmastower.Assets;
import com.noxalus.xmastower.Config;
import com.noxalus.xmastower.State;
import com.noxalus.xmastower.XmasTower;
import com.noxalus.xmastower.entities.Gift;
import com.noxalus.xmastower.entities.SpriteActor;

import java.util.ArrayList;

public class GameScreen extends ApplicationAdapter implements InputProcessor, Screen {

    private static final String TAG = "GameScreen";

    private XmasTower _game;

    private ArrayList<Gift> _gifts = new ArrayList<Gift>();
    Vector2 _cameraTarget;
    BitmapFont _font;
    boolean _needToAddNewGift;
    boolean _cameraIsMoving;
    public int _score;
    private int _bestScore;
    Sound _currentPlayedSound;
    public boolean GameWillReset;
    private Preferences _preferences;
    private SpriteActor _groundSpriteActor;

    // Physics
    Body _groundBody;

    public GameScreen(XmasTower game)
    {
        _game = game;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);

        _font = new BitmapFont();

        _groundSpriteActor = new SpriteActor(new Sprite(Assets.groundTexture));
        _groundSpriteActor.setScale(1f, 1f);
        _groundSpriteActor.setPosition(
                -(_groundSpriteActor.getWidth() * _groundSpriteActor.getScaleX()) / 2f,
                -Gdx.graphics.getHeight() / 2f
        );

        _preferences = Gdx.app.getPreferences("xmas-tower");
        _bestScore = _preferences.getInteger("highscore", 0);

        initializePhysics();

        reset();
    }

    public void reset() {
        _game.Camera.position.set(0, 0, 0);
        _cameraTarget = new Vector2(_game.Camera.position.x, _game.Camera.position.y);
        _needToAddNewGift = false;
        GameWillReset = false;
        _game.Stage.clear();

        _game.Stage.addActor(_groundSpriteActor);

        if (_score > _bestScore) {
            _preferences.putInteger("highscore", _score);
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

        Gift gift = new Gift(_game, new Vector2(
                0f,
                _game.Camera.position.y + (Gdx.graphics.getHeight() / 2f) - 100f
            )
        );
        gift.initializePhysics(_game.World);
        _game.Stage.addActor(gift);
        gift.setZIndex(0);
        _gifts.add(gift);
    }

    public void gameFinished() {
        _game.pausePhysics(true);
        translateCamera(new Vector2(0, -(_game.Camera.position.y - _groundBody.getPosition().y)));
        GameWillReset = true;
    }

    public void update(float delta) {

        if (!GameWillReset) {
            for (int i = 0; i < _gifts.size(); i++) {
                Gift currentGift = _gifts.get(i);

                // Outside of the scene?
                if (currentGift.getX() < -Gdx.graphics.getWidth() / 2f - currentGift.getBox().sprite.getWidth() ||
                        currentGift.getX() > Gdx.graphics.getWidth() / 2 ||
                        currentGift.getY() < -Gdx.graphics.getHeight()) {
                    _game.GameScreen.gameFinished();
                } else if (!currentGift.isPlaced() && !currentGift.isMovable() &&
                        currentGift.getBody().getLinearVelocity().x < Config.LINEAR_VELOCITY_THRESHOLD &&
                        currentGift.getBody().getLinearVelocity().x > -Config.LINEAR_VELOCITY_THRESHOLD &&
                        currentGift.getBody().getLinearVelocity().y < Config.LINEAR_VELOCITY_THRESHOLD &&
                        currentGift.getBody().getLinearVelocity().y > -Config.LINEAR_VELOCITY_THRESHOLD) {
                    Gdx.app.log("GIFT", "HAS STOP TO MOVE");
                    currentGift.isPlaced(true);

                    currentGift.switchState(State.SLEEPING);

                    Vector3 screenCoordinates = _game.Camera.project(
                            new Vector3(currentGift.getX(), currentGift.getY(), 0.f)
                    );
                    float limitThreshold = Gdx.graphics.getWidth() / 1.5f;

                    if (screenCoordinates.y > limitThreshold)
                        _game.GameScreen.translateCamera(new Vector2(0f, Gdx.graphics.getWidth() / 2f));

                    _game.GameScreen._score++;
                    _game.GameScreen.addGift();
                }

                currentGift.update(Gdx.graphics.getDeltaTime());
            }
        }

        updateCamera(Gdx.graphics.getDeltaTime());
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

        _game.SpriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        _game.SpriteBatch.begin();
        _font.draw(_game.SpriteBatch, Integer.toString(_score), Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight());
        _font.draw(_game.SpriteBatch, Integer.toString(_bestScore), Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - _font.getLineHeight());
        _game.SpriteBatch.end();
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

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    /** we instantiate this vector and the callback here so we don't irritate the GC **/
    Vector3 testPoint = new Vector3();
    QueryCallback callback = new QueryCallback() {
        @Override
        public boolean reportFixture (Fixture fixture) {
            Gift selectedGift = (Gift)(fixture.getBody().getUserData());

            if (selectedGift == null || !selectedGift.isMovable())
                return true;

            selectedGift.isSelected(true);

            // if the hit point is inside the fixture of the body we report it
            if (fixture.testPoint(testPoint.x, testPoint.y)) {
                _game.HitBody = fixture.getBody();
                return false;
            } else {
                return true;
            }
        }
    };

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        //body.applyForceToCenter(0f,10f,true);
        Gdx.app.log(TAG, "Touch position: " + x + ", " + y);

        if (pointer > 0 || _game.MouseJoint != null)
            return false;

        // translate the mouse coordinates to World coordinates
        testPoint.set(x, y, 0);
        _game.Camera.unproject(testPoint);
        testPoint.set(testPoint.x / Config.PIXELS_TO_METERS, testPoint.y / Config.PIXELS_TO_METERS, 0);

        // ask the World which bodies are within the given
        // bounding box around the mouse pointer
        _game.HitBody = null;
        float value = 0.1f;
        _game.World.QueryAABB(callback, testPoint.x - value, testPoint.y - value, testPoint.x + value, testPoint.y + value);
        // if we hit something we create a new mouse joint
        // and attach it to the hit body.
        if (_game.MouseJoint == null && _game.HitBody != null) {
            _currentPlayedSound = Assets.grabSounds[MathUtils.random(0,  Assets.grabSounds.length - 1)];
            _currentPlayedSound.play();
            MouseJointDef def = new MouseJointDef();
            def.bodyA = _groundBody;
            def.bodyB = _game.HitBody;
            def.collideConnected = true;
            def.target.set(testPoint.x, testPoint.y);
            def.maxForce = (10000.0f / Config.PIXELS_TO_METERS) * _game.HitBody.getMass();

            Gdx.app.log(TAG, "Create a new mouse joint");
            _game.MouseJoint = (MouseJoint) _game.World.createJoint(def);
            _game.HitBody.setAwake(true);
        }

        return false;
    }

    Vector2 target = new Vector2();

    @Override
    public boolean touchDragged (int x, int y, int pointer) {

        Gdx.app.log(TAG, "Touch dragged pointer: " + pointer);
        Gdx.app.log(TAG, "Touch dragged: " + x + ", " + y);


        if (pointer > 0)
            return false;


        if (_game.MouseJoint != null) {
            _game.Camera.unproject(testPoint.set(x, y, 0));
            _game.MouseJoint.setTarget(target.set(
                    testPoint.x / Config.PIXELS_TO_METERS,
                    testPoint.y / Config.PIXELS_TO_METERS
            ));
        }

        return false;
    }

    @Override
    public boolean touchUp (int x, int y, int pointer, int button) {
        if (_currentPlayedSound != null)
        {
            _currentPlayedSound.stop();
            _currentPlayedSound = null;
        }
        if (_game.MouseJoint != null && _game.HitBody != null) {
            Gdx.app.log(TAG, "Remove mouse joint from touch up");

            ((Gift)(_game.HitBody.getUserData())).isSelected(false);
            _game.World.destroyJoint(_game.MouseJoint);
            _game.MouseJoint = null;
            _game.HitBody = null;
        }

        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
