package com.noxalus.xmastower;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

	// Physics
	World world;
	Body groundBody;
	Box2DDebugRenderer debugRenderer;
	Matrix4 debugMatrix;
	private MouseJoint mouseJoint = null;
	Body hitBody = null;
	float physicsUpdateTime = 0f;

	// Particles
	ParticleEffectPool snowRainEffectPool;
	Array<ParticleEffectPool.PooledEffect> effects = new Array();

	@Override
	public void create () {
		_batch = new SpriteBatch();
		Assets.load();

		initializeParticles();
		initializePhysics();

		Gdx.input.setInputProcessor(this);

		_font = new BitmapFont();
		_camera = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

		reset();
	}

	public void reset() {
		_camera.position.set(0, 0, 0);
		_cameraTarget = new Vector2(_camera.position.x, _camera.position.y);
		_needToAddNewGift = false;

		Assets.music.stop();
		Assets.music.play();

		for (Gift g : _gifts)
		{
			world.destroyBody(g.getBody());
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

		snowRainEffectPool = new ParticleEffectPool(snowRainEffect, 1, 2);

		// Create effect:
		ParticleEffectPool.PooledEffect effect = snowRainEffectPool.obtain();
		effect.setPosition(Gdx.graphics.getWidth() / 2.f, Gdx.graphics.getHeight() + 200.f);
		effects.add(effect);
	}

	private void initializePhysics() {
		world = new World(new Vector2(0, -9.8f), true);

		BodyDef bodyGround = new BodyDef();
		bodyGround.type = BodyDef.BodyType.StaticBody;
		float w = Gdx.graphics.getWidth() / Config.PIXELS_TO_METERS;
		float h = Gdx.graphics.getHeight() / Config.PIXELS_TO_METERS - 250 / Config.PIXELS_TO_METERS;
		bodyGround.position.set(0, 0);
		FixtureDef fixtureGround = new FixtureDef();

		EdgeShape edgeShape = new EdgeShape();
		edgeShape.set(-w/2,-h/2,w/2,-h/2);
		fixtureGround.shape = edgeShape;

		groundBody = world.createBody(bodyGround);
		groundBody.createFixture(fixtureGround);
		edgeShape.dispose();

		// Create a Box2DDebugRenderer, this allows us to see the physics simulation controlling the scene
		debugRenderer = new Box2DDebugRenderer();

		// You can savely ignore the rest of this method :)
		world.setContactListener(new ContactListener() {
			@Override
			public void beginContact(Contact contact) {
				Gdx.app.log(TAG, "begin contact");
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
		gift.initializePhysics(world);
		_gifts.add(gift);
	}

	public void update() {
		long start = TimeUtils.nanoTime();
		// Step the physics simulation forward at a rate of 60hz
		world.step(1f / 60f, 6, 2);
		physicsUpdateTime = (TimeUtils.nanoTime() - start) / 1000000000.0f;

		for (int i = 0; i < _gifts.size(); i++)
		{
			_gifts.get(i).update(Gdx.graphics.getDeltaTime());
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

		// Update and draw effects:
		for (int i = effects.size - 1; i >= 0; i--) {
			ParticleEffectPool.PooledEffect effect = effects.get(i);
			effect.draw(_batch, Gdx.graphics.getDeltaTime());
			if (effect.isComplete()) {
				effect.free();
				effects.removeIndex(i);
			}
		}

		_batch.end();

		_batch.setProjectionMatrix(_camera.combined);
		// Scale down the sprite batches projection matrix to box2D size
		debugMatrix = _batch.getProjectionMatrix().cpy().scale(Config.PIXELS_TO_METERS, Config.PIXELS_TO_METERS, 0);

		_batch.begin();

		for (int i = 0; i < _gifts.size(); i++)
		{
			_gifts.get(i).draw(Gdx.graphics.getDeltaTime(), _batch);
		}

		_batch.end();

		_batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		_batch.begin();
		_font.draw(_batch, "FPS: " + Gdx.graphics.getFramesPerSecond() + " | Physics update time: " + physicsUpdateTime, 0, 20);
		_batch.end();

		// Now render the physics world using our scaled down matrix
		// Note, this is strictly optional and is, as the name suggests, just for debugging purposes
		debugRenderer.render(world, debugMatrix);
	}

	@Override
	public void render () {
		this.update();
		this.draw();
	}

	@Override
	public void dispose() {
		world.dispose();
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
			Gdx.app.log(TAG, "reportFixture");

			Gift selectedGift = (Gift)(fixture.getBody().getUserData());

			if (selectedGift == null || !selectedGift.isMovable())
				return true;

			selectedGift.isSelected(true);

			// if the hit point is inside the fixture of the body we report it
			if (fixture.testPoint(testPoint.x, testPoint.y)) {
				hitBody = fixture.getBody();
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

		// translate the mouse coordinates to world coordinates
		testPoint.set(x, y, 0);
		_camera.unproject(testPoint);
		testPoint.set(testPoint.x / Config.PIXELS_TO_METERS, testPoint.y / Config.PIXELS_TO_METERS, 0);

		// ask the world which bodies are within the given
		// bounding box around the mouse pointer
		hitBody = null;
		float value = 0.1f;
		world.QueryAABB(callback, testPoint.x - value, testPoint.y - value, testPoint.x + value, testPoint.y + value);
		// if we hit something we create a new mouse joint
		// and attach it to the hit body.
		if (hitBody != null) {
			Gdx.app.log(TAG, "hitBody != null");
			MouseJointDef def = new MouseJointDef();
			def.bodyA = groundBody;
			def.bodyB = hitBody;
			def.collideConnected = true;
			def.target.set(testPoint.x, testPoint.y);
			def.maxForce = (10000.0f / Config.PIXELS_TO_METERS) * hitBody.getMass();

			mouseJoint = (MouseJoint)world.createJoint(def);
			hitBody.setAwake(true);
		}

		return false;
	}

	Vector2 target = new Vector2();

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		// if a mouse joint exists we simply update
		// the target of the joint based on the new
		// mouse coordinates
		if (mouseJoint != null) {
			_camera.unproject(testPoint.set(x, y, 0));
			mouseJoint.setTarget(target.set(testPoint.x / Config.PIXELS_TO_METERS, testPoint.y / Config.PIXELS_TO_METERS));
		}

		return false;
	}

	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		// if a mouse joint exists we simply destroy it
		if (mouseJoint != null) {
			((Gift)(hitBody.getUserData())).isSelected(false);
			world.destroyJoint(mouseJoint);
			mouseJoint = null;
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
