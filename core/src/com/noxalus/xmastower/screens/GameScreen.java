package com.noxalus.xmastower.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
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
    private Stage _stage;
    private ArrayList<Gift> _gifts = new ArrayList<Gift>();
    public OrthographicCamera _camera;
    Vector2 _cameraTarget;
    BitmapFont _font;
    boolean _needToAddNewGift;
    boolean _cameraIsMoving;
    public int _score;
    private int _bestScore;
    Sound _currentPlayedSound;
    private Viewport _viewport;
    public boolean GameWillReset;
    private Preferences _preferences;
    private SpriteActor _groundSpriteActor;

    // Physics
    World _world;
    Body _groundBody;
    float _groundHeight = 250f;
    Box2DDebugRenderer _debugRenderer;
    Matrix4 _debugMatrix;
    private MouseJoint _mouseJoint = null;
    Body _hitBody = null;
    float _physicsUpdateTime = 0f;
    boolean _destroyMouseJoint;
    boolean _physicsPaused;

    public GameScreen(XmasTower game)
    {
        _game = game;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);

        _font = new BitmapFont();
        _camera = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        _viewport = new ScreenViewport(_camera);

        _stage = new Stage(_viewport);

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
        _camera.position.set(0, 0, 0);
        _cameraTarget = new Vector2(_camera.position.x, _camera.position.y);
        _needToAddNewGift = false;
        GameWillReset = false;
        _stage.clear();

        _stage.addActor(_groundSpriteActor);

        if (_score > _bestScore) {
            _preferences.putInteger("highscore", _score);
            _preferences.flush();
            _bestScore = _score;
        }

        _score = 0;

        _destroyMouseJoint = false;
        _physicsPaused = false;
        _currentPlayedSound = null;

        Assets.gameMusicLoop.stop();
        Assets.gameMusicIntro.stop();
        Assets.gameMusicIntro.play();

        for (Gift g : _gifts)
        {
            _world.destroyBody(g.getBody());
        }

        _gifts.clear();

        addGift();
    }

    private void initializePhysics() {
        _world = new World(new Vector2(0.f, -19.8f), true);

        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.type = BodyDef.BodyType.StaticBody;

        groundBodyDef.position.set(
            (_groundSpriteActor.getX() + (_groundSpriteActor.sprite.getWidth() / 2f) *
                    _groundSpriteActor.getScaleX()) / Config.PIXELS_TO_METERS,
            (_groundSpriteActor.getY() + (_groundSpriteActor.sprite.getHeight() / 2f) *
                    _groundSpriteActor.getScaleY()) / Config.PIXELS_TO_METERS
        );

        _groundBody = _world.createBody(groundBodyDef);

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

        // Create a Box2DDebugRenderer, this allows us to see the physics simulation controlling the scene
        _debugRenderer = new Box2DDebugRenderer();

        // You can savely ignore the rest of this method :)
        _world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Gdx.app.log(TAG, "begin contact");

                if (_mouseJoint != null && _hitBody != null) {
                    Gdx.app.log(TAG, "Remove mouse joint from collision");

                    Gift selectedGift = (Gift) _hitBody.getUserData();
                    selectedGift.isSelected(false);
                    selectedGift.isMovable(false);

                    // We can't destroy a joint in this callback directly
                    // that's why it's delayed
                    _destroyMouseJoint = true;

                    _hitBody = null;
                }

                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();

                Gift giftA = (Gift) (fixtureA.getBody().getUserData());
                Gift giftB = (Gift) (fixtureB.getBody().getUserData());

                if (giftA != null) {
                    if (giftA.getBody().getLinearVelocity().y < -10)
                        Assets.ouchSounds[MathUtils.random(Assets.ouchSounds.length - 1)].play();

                    Gdx.app.log(TAG, "Gift A linear velocity: " + giftA.getBody().getLinearVelocity());

                    giftA.isMovable(false);
                    giftA.isSelected(false);
                    giftA.switchState(State.COLLISIONING);
                }

                if (giftB != null) {
                    if (giftB.getBody().getLinearVelocity().y < -10)
                        Assets.ouchSounds[MathUtils.random(Assets.ouchSounds.length - 1)].play();
                    Gdx.app.log(TAG, "Gift B linear velocity: " + giftB.getBody().getLinearVelocity());

                    giftB.isMovable(false);
                    giftB.isSelected(false);
                    giftB.switchState(State.COLLISIONING);
                }
            }

            @Override
            public void endContact(Contact contact) {
                Gdx.app.log(TAG, "end contact");

                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();

                Gift giftA = (Gift) (fixtureA.getBody().getUserData());
                if (giftA != null) {
                    giftA.switchState(State.IDLE);
                }

                Gift giftB = (Gift) (fixtureB.getBody().getUserData());
                if (giftB != null) {
                    giftB.switchState(State.IDLE);
                }
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                Manifold.ManifoldType type = oldManifold.getType();
                Vector2 localPoint = oldManifold.getLocalPoint();
                Vector2 localNormal = oldManifold.getLocalNormal();
                int pointCount = oldManifold.getPointCount();
                Manifold.ManifoldPoint[] points = oldManifold.getPoints();
//				Gdx.app.log(TAG, "pre solve, " + type +
//						", point: " + localPoint +
//						", local normal: " + localNormal +
//						", #points: " + pointCount +
//						", [" + points[0] + ", " + points[1] + "]");
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
                float[] ni = impulse.getNormalImpulses();
                float[] ti = impulse.getTangentImpulses();

                if (ti[1] > 1.f)
                {
                    Gdx.app.log(TAG, "post solve, normal impulses: " + ni[0] + ", " + ni[1] + ", tangent impulses: " + ti[0] + ", " + ti[1]);
                }
            }
        });
    }

    public void addGift() {

        if (_cameraIsMoving) {
            _needToAddNewGift = true;
            return;
        }

        Gift gift = new Gift(this, new Vector2(
                0f,
                _camera.position.y + (Gdx.graphics.getHeight() / 2f) - 100f
            )
        );
        gift.initializePhysics(_world);
        _stage.addActor(gift);
        gift.setZIndex(0);
        _gifts.add(gift);
    }

    public void gameFinished() {
        _physicsPaused = true;
        translateCamera(new Vector2(0, -(_camera.position.y - _groundBody.getPosition().y)));
        GameWillReset = true;
    }

    public void update(float delta) {
        long start = TimeUtils.nanoTime();

        // Music intro will finish?
//        if (!Assets.gameMusicLoop.isPlaying() && Assets.gameMusicIntro.getPosition() > 3.75f)
//            Assets.gameMusicLoop.play();

        if (!_physicsPaused) {
            // Step the physics simulation forward at a rate of 60hz
            _world.step(1f / 60f, 6, 2);
            _physicsUpdateTime = (TimeUtils.nanoTime() - start) / 1000000000.0f;
        }

        for (int i = 0; i < _gifts.size(); i++)
        {
            _gifts.get(i).update(Gdx.graphics.getDeltaTime());
        }

        _stage.act(Gdx.graphics.getDeltaTime());

        if (_destroyMouseJoint)
        {
            _destroyMouseJoint = false;

            if (_mouseJoint != null) {
                _world.destroyJoint(_mouseJoint);
                _mouseJoint = null;
            }
        }

        updateCamera(Gdx.graphics.getDeltaTime());
    }

    private void updateCamera(float delta) {
        Vector3 position = _camera.position;

        float cameraInterpolationThreshold = 10.f;
        if (Math.abs(position.x - _cameraTarget.x) > cameraInterpolationThreshold ||
                Math.abs(position.y - _cameraTarget.y) > cameraInterpolationThreshold) {
            float lerp = 0.05f;

            position.x = position.x + (_cameraTarget.x - position.x) * lerp;
            position.y = position.y + (_cameraTarget.y - position.y) * lerp;

            _camera.position.set(position);
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

        _camera.update();
    }

    public void translateCamera(Vector2 target) {
        _cameraIsMoving = true;
        _cameraTarget = new Vector2(_camera.position.x, _camera.position.y).add(target);
    }

    public void draw(float delta) {
        _game.SpriteBatch.setProjectionMatrix(_camera.combined);
        // Scale down the sprite batches projection matrix to box2D size
        _debugMatrix = _game.SpriteBatch.getProjectionMatrix().cpy().scale(Config.PIXELS_TO_METERS, Config.PIXELS_TO_METERS, 0);

        _game.SpriteBatch.begin();
        _stage.draw();
        _game.SpriteBatch.end();

        _game.SpriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        _game.SpriteBatch.begin();
        _font.draw(_game.SpriteBatch, Integer.toString(_score), Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight());
        _font.draw(_game.SpriteBatch, Integer.toString(_bestScore), Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - _font.getLineHeight());
        _font.draw(_game.SpriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond() + " | Physics update time: " + _physicsUpdateTime, 0, 20);
        _game.SpriteBatch.end();

        // Now render the physics _world using our scaled down matrix
        // Note, this is strictly optional and is, as the name suggests, just for debugging purposes
//		_debugRenderer.render(_world, _debugMatrix);
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw(delta);
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log(TAG, "Resize: " + width + ", " + height);
        _viewport.update(width, height);
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
        _world.dispose();
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
                _hitBody = fixture.getBody();
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

        if (pointer > 0 || _mouseJoint != null)
            return false;

        // translate the mouse coordinates to _world coordinates
        testPoint.set(x, y, 0);
        _camera.unproject(testPoint);
        testPoint.set(testPoint.x / Config.PIXELS_TO_METERS, testPoint.y / Config.PIXELS_TO_METERS, 0);

        // ask the _world which bodies are within the given
        // bounding box around the mouse pointer
        _hitBody = null;
        float value = 0.1f;
        _world.QueryAABB(callback, testPoint.x - value, testPoint.y - value, testPoint.x + value, testPoint.y + value);
        // if we hit something we create a new mouse joint
        // and attach it to the hit body.
        if (_mouseJoint == null && _hitBody != null) {
            _currentPlayedSound = Assets.grabSounds[MathUtils.random(0,  Assets.grabSounds.length - 1)];
            _currentPlayedSound.play();
            MouseJointDef def = new MouseJointDef();
            def.bodyA = _groundBody;
            def.bodyB = _hitBody;
            def.collideConnected = true;
            def.target.set(testPoint.x, testPoint.y);
            def.maxForce = (10000.0f / Config.PIXELS_TO_METERS) * _hitBody.getMass();

            Gdx.app.log(TAG, "Create a new mouse joint");
            _mouseJoint = (MouseJoint) _world.createJoint(def);
            _hitBody.setAwake(true);
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


        if (_mouseJoint != null) {
            _camera.unproject(testPoint.set(x, y, 0));
            _mouseJoint.setTarget(target.set(
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
        if (_mouseJoint != null && _hitBody != null) {
            Gdx.app.log(TAG, "Remove mouse joint from touch up");

            ((Gift)(_hitBody.getUserData())).isSelected(false);
            _world.destroyJoint(_mouseJoint);
            _mouseJoint = null;
            _hitBody = null;
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
