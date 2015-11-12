package com.noxalus.xmastower;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noxalus.xmastower.entities.Gift;

import java.util.ArrayList;

public class XmasTower extends ApplicationAdapter implements InputProcessor {

	private static final String TAG = "XmasTower";

	SpriteBatch _batch;
	private ArrayList<Gift> _gifts = new ArrayList<Gift>();
	public OrthographicCamera _camera;
	Vector2 _cameraTarget;
	BitmapFont _font;
	boolean _needToAddNewGift;
	boolean _cameraIsMoving;
	public int _score;
	Sound _currentPlayedSound;
	private Viewport _viewport;

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

	// Particles
	ParticleEffectPool _snowRainEffectPool;
	Array<ParticleEffectPool.PooledEffect> _effects = new Array();
	float _startParticlesTime = 8f;

	@Override
	public void create () {
		_batch = new SpriteBatch();
		Assets.load();

		initializeParticles();
		initializePhysics();

		Gdx.input.setInputProcessor(this);

		_font = new BitmapFont();
		_camera = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
		_viewport = new ScreenViewport(_camera);

		reset();
	}

	public void resize(int width, int height) {
		Gdx.app.log(TAG, "Resize: " + width + ", " + height);
		_viewport.update(width, height);
	}

	public void reset() {
		_camera.position.set(0, 0, 0);
		_cameraTarget = new Vector2(_camera.position.x, _camera.position.y);
		_needToAddNewGift = false;
		_score = 0;
		_destroyMouseJoint = false;
		_currentPlayedSound = null;

		Assets.music.stop();
		Assets.music.play();

		for (Gift g : _gifts)
		{
			_world.destroyBody(g.getBody());
		}

		_gifts.clear();

		addGift();
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

		effect.update(_startParticlesTime);
	}

	private void initializePhysics() {
		_world = new World(new Vector2(0.f, -19.8f), true);

		BodyDef bodyGround = new BodyDef();
		bodyGround.type = BodyDef.BodyType.StaticBody;
		float w = Gdx.graphics.getWidth() / Config.PIXELS_TO_METERS;
		float h = Gdx.graphics.getHeight() / Config.PIXELS_TO_METERS - _groundHeight / Config.PIXELS_TO_METERS;
		bodyGround.position.set(0, 0);
		FixtureDef fixtureGround = new FixtureDef();

		EdgeShape edgeShape = new EdgeShape();
		edgeShape.set(-w / 2, -h / 2, w / 2, -h / 2);
		fixtureGround.shape = edgeShape;

		_groundBody = _world.createBody(bodyGround);
		_groundBody.createFixture(fixtureGround);
		edgeShape.dispose();

		// Create a Box2DDebugRenderer, this allows us to see the physics simulation controlling the scene
		_debugRenderer = new Box2DDebugRenderer();

		// You can savely ignore the rest of this method :)
		_world.setContactListener(new ContactListener() {
			@Override
			public void beginContact(Contact contact) {
				Gdx.app.log(TAG, "begin contact");

				if (_mouseJoint != null && _hitBody != null) {
					Gdx.app.log(TAG, "Remove mouse joint from collision");

					Gift selectedGift = (Gift)_hitBody.getUserData();
					selectedGift.isSelected(false);
					selectedGift.isMovable(false);

					// We can't destroy a joint in this callback directly
					// that's why it's delayed
					_destroyMouseJoint = true;

					_hitBody = null;
				}

				Fixture fixtureA = contact.getFixtureA();
				Fixture fixtureB = contact.getFixtureB();

				Gift giftA = (Gift)(fixtureA.getBody().getUserData());
				if (giftA != null) {
					giftA.isMovable(false);
					giftA.isSelected(false);
				}

				Gift giftB = (Gift)(fixtureB.getBody().getUserData());
				if (giftB != null) {
					giftB.isMovable(false);
					giftB.isSelected(false);
				}
			}

			@Override
			public void endContact(Contact contact) {
				Gdx.app.log(TAG, "end contact");
			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
				Manifold.ManifoldType type = oldManifold.getType();
				Vector2 localPoint = oldManifold.getLocalPoint();
				Vector2 localNormal = oldManifold.getLocalNormal();
				int pointCount = oldManifold.getPointCount();
				Manifold.ManifoldPoint[] points = oldManifold.getPoints();
				Gdx.app.log(TAG, "pre solve, " + type +
						", point: " + localPoint +
						", local normal: " + localNormal +
						", #points: " + pointCount +
						", [" + points[0] + ", " + points[1] + "]");
			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
				float[] ni = impulse.getNormalImpulses();
				float[] ti = impulse.getTangentImpulses();
				Gdx.app.log(TAG, "post solve, normal impulses: " + ni[0] + ", " + ni[1] + ", tangent impulses: " + ti[0] + ", " + ti[1]);
			}
		});
	}

	public void addGift() {

		if (_cameraIsMoving) {
			_needToAddNewGift = true;
			return;
		}

		Gift gift = new Gift(this, new Vector2(
				_camera.position.x + -Assets.giftTexture.getWidth() / 2f,
				_camera.position.y + Assets.giftTexture.getHeight() / 2f)
		);
		gift.initializePhysics(_world);
		_gifts.add(gift);
	}

	public void update() {
		long start = TimeUtils.nanoTime();
		// Step the physics simulation forward at a rate of 60hz
		_world.step(1f / 60f, 6, 2);
		_physicsUpdateTime = (TimeUtils.nanoTime() - start) / 1000000000.0f;

		for (int i = 0; i < _gifts.size(); i++)
		{
			_gifts.get(i).update(Gdx.graphics.getDeltaTime());
		}

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
		}

		_camera.update();
	}

	public void translateCamera(Vector2 target) {
		_cameraIsMoving = true;
		_cameraTarget = new Vector2(_camera.position.x, _camera.position.y).add(target);
	}

	public void draw() {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		_batch.begin();

		// Update and draw _effects:
		for (int i = _effects.size - 1; i >= 0; i--) {
			ParticleEffectPool.PooledEffect effect = _effects.get(i);
			effect.draw(_batch, Gdx.graphics.getDeltaTime());
			if (effect.isComplete()) {
				effect.free();
				_effects.removeIndex(i);
			}
		}

		_batch.end();

		_batch.setProjectionMatrix(_camera.combined);
		// Scale down the sprite batches projection matrix to box2D size
		_debugMatrix = _batch.getProjectionMatrix().cpy().scale(Config.PIXELS_TO_METERS, Config.PIXELS_TO_METERS, 0);

		_batch.begin();

		for (int i = _gifts.size() - 1; i >= 0; i--)
		{
			_gifts.get(i).draw(Gdx.graphics.getDeltaTime(), _batch);
		}

		_batch.end();

		_batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		_batch.begin();
		_font.draw(_batch, Integer.toString(_score), Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight());
		_font.draw(_batch, "FPS: " + Gdx.graphics.getFramesPerSecond() + " | Physics update time: " + _physicsUpdateTime, 0, 20);
		_batch.end();

		// Now render the physics _world using our scaled down matrix
		// Note, this is strictly optional and is, as the name suggests, just for debugging purposes
		_debugRenderer.render(_world, _debugMatrix);
	}

	@Override
	public void render () {
		this.update();
		this.draw();
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
		if (_hitBody != null) {
			_currentPlayedSound = Assets.grabSounds[MathUtils.random(0,  Assets.grabSounds.length - 1)];
			_currentPlayedSound.play();
			MouseJointDef def = new MouseJointDef();
			def.bodyA = _groundBody;
			def.bodyB = _hitBody;
			def.collideConnected = true;
			def.target.set(testPoint.x, testPoint.y);
			def.maxForce = (10000.0f / Config.PIXELS_TO_METERS) * _hitBody.getMass();

			_mouseJoint = (MouseJoint) _world.createJoint(def);
			_hitBody.setAwake(true);
		}

		return false;
	}

	Vector2 target = new Vector2();

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
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
