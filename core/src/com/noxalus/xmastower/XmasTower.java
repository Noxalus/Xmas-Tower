package com.noxalus.xmastower;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noxalus.xmastower.entities.Gift;
import com.noxalus.xmastower.screens.MenuScreen;
import com.noxalus.xmastower.screens.GameScreen;

public class XmasTower extends Game {

	private static final String TAG = "XmasTower";

	public SpriteBatch SpriteBatch;

	public MenuScreen MenuScreen;
	public GameScreen GameScreen;

    public Stage Stage;
    public Viewport Viewport;

    // Camera
    public OrthographicCamera Camera;

    // Fonts
    BitmapFont _font;

    // Particles
    ParticleEffectPool _snowRainEffectPool;
    Array<ParticleEffectPool.PooledEffect> _effects = new Array();

    // Physics
    public World World;
    private Box2DDebugRenderer _debugRenderer;
    private Matrix4 _debugMatrix;
    public MouseJoint MouseJoint = null;
    public Body HitBody = null;
    private boolean _physicsPaused = false;
    private float _physicsUpdateTime = 0f;
    public boolean DestroyMouseJoint = false;

    public void pausePhysics(boolean value)
    {
        _physicsPaused = value;
    }

    @Override
	public void create () {
		SpriteBatch = new SpriteBatch();
		Assets.load();

        Camera = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        Viewport = new ScreenViewport(Camera);
        Stage = new Stage(Viewport);
        _font = new BitmapFont();

        MenuScreen = new MenuScreen(this);
		GameScreen = new GameScreen(this);

		setScreen(MenuScreen);

        initializeParticles();
        initializePhysics();
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log(TAG, "Resize: " + width + ", " + height);
        Viewport.update(width, height);
    }

    private void initializeParticles() {

        //Set up the particle effect that will act as the pool's template
        ParticleEffect snowRainEffect = new ParticleEffect();
        snowRainEffect.load(
            Gdx.files.internal("graphics/particles/snow2.p"),
            Gdx.files.internal("graphics/pictures")
        );

        //If your particle effect includes additive or pre-multiplied particle emitters
        //you can turn off blend function clean-up to save a lot of draw calls, but
        //remember to switch the Batch back to GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA
        //before drawing "regular" sprites or your Stage.
        snowRainEffect.setEmittersCleanUpBlendFunction(true);

        _snowRainEffectPool = new ParticleEffectPool(snowRainEffect, 1, 2);

        // Create effect:
        ParticleEffectPool.PooledEffect effect = _snowRainEffectPool.obtain();
        effect.setPosition(Gdx.graphics.getWidth() / 2.f, Gdx.graphics.getHeight() + 200.f);
        _effects.add(effect);
    }

    private void initializePhysics() {
        World = new World(new Vector2(0.f, -19.8f), true);

        // Create a Box2DDebugRenderer, this allows us to see the physics simulation controlling the scene
        _debugRenderer = new Box2DDebugRenderer();

        // You can savely ignore the rest of this method :)
        World.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Gdx.app.log(TAG, "begin contact");

                if (MouseJoint != null && HitBody != null) {
                    Gdx.app.log(TAG, "Remove mouse joint from collision");

                    Gift selectedGift = (Gift) HitBody.getUserData();
                    selectedGift.isSelected(false);
                    selectedGift.isMovable(false);

                    // We can't destroy a joint in this callback directly
                    // that's why it's delayed
                    DestroyMouseJoint = true;

                    HitBody = null;
                }

                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();

                Gift giftA = (Gift) (fixtureA.getBody().getUserData());
                Gift giftB = (Gift) (fixtureB.getBody().getUserData());

                if (giftA != null) {
                    if (giftA.getBody().getLinearVelocity().y < -10) {
                        Assets.ouchSounds[MathUtils.random(Assets.ouchSounds.length - 1)].play();
                        giftA.switchState(State.COLLISIONING);
                    }

                    Gdx.app.log(TAG, "Gift A linear velocity: " + giftA.getBody().getLinearVelocity());

                    giftA.isMovable(false);
                    giftA.isSelected(false);
                }

                if (giftB != null) {
                    if (giftB.getBody().getLinearVelocity().y < -10) {
                        Assets.ouchSounds[MathUtils.random(Assets.ouchSounds.length - 1)].play();
                        giftB.switchState(State.COLLISIONING);
                    }

                    Gdx.app.log(TAG, "Gift B linear velocity: " + giftB.getBody().getLinearVelocity());

                    giftB.isMovable(false);
                    giftB.isSelected(false);
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

                if (ti[1] > 1.f) {
                    Gdx.app.log(TAG, "post solve, normal impulses: " + ni[0] + ", " + ni[1] + ", tangent impulses: " + ti[0] + ", " + ti[1]);
                }
            }
        });
    }

    public void update() {
        long start = TimeUtils.nanoTime();

        if (!_physicsPaused) {
            // Step the physics simulation forward at a rate of 60hz
            World.step(1f / 60f, 6, 2);
            _physicsUpdateTime = (TimeUtils.nanoTime() - start) / 1000000000.0f;
        }

        Stage.act(Gdx.graphics.getDeltaTime());

        if (DestroyMouseJoint)
        {
            DestroyMouseJoint = false;

            if (MouseJoint != null) {
                World.destroyJoint(MouseJoint);
                MouseJoint = null;
            }
        }

        Camera.update();
    }

    public void draw() {
        SpriteBatch.begin();

        // Update and draw _effects:
        for (int i = _effects.size - 1; i >= 0; i--) {
            ParticleEffectPool.PooledEffect effect = _effects.get(i);
            effect.draw(SpriteBatch, Gdx.graphics.getDeltaTime());
            if (effect.isComplete()) {
                effect.free();
                _effects.removeIndex(i);
            }
        }

        SpriteBatch.end();

        Stage.draw();

        SpriteBatch.setProjectionMatrix(Camera.combined);
        // Scale down the sprite batches projection matrix to box2D size
        _debugMatrix = SpriteBatch.getProjectionMatrix().cpy().scale(Config.PIXELS_TO_METERS, Config.PIXELS_TO_METERS, 0);

        // Now render the physics World using our scaled down matrix
        // Note, this is strictly optional and is, as the name suggests, just for debugging purposes
        _debugRenderer.render(World, _debugMatrix);

        SpriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        SpriteBatch.begin();
        _font.draw(SpriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond() + " | Physics update time: " + _physicsUpdateTime, 0, 20);
        SpriteBatch.end();
    }

    @Override
    public void render () {
        Gdx.gl.glClearColor(0.13f, 0.5f, 0.8f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update();
        draw();

        super.render();
    }
}
