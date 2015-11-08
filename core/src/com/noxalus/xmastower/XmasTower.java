package com.noxalus.xmastower;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.Sprite;
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
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;

public class XmasTower extends ApplicationAdapter implements InputProcessor {

	private static final String TAG = "XmasTower";

	SpriteBatch batch;
	Sprite sprite, sprite2;
	Texture img;
	Music music;
	World world;
	private ArrayList<Body> boxes = new ArrayList<Body>();
	Body groundBody;
	Box2DDebugRenderer debugRenderer;
	Matrix4 debugMatrix;
	OrthographicCamera camera;
	BitmapFont font;
	private MouseJoint mouseJoint = null;
	Body hitBody = null;
	boolean drawSprite = true;

	// Particles
	ParticleEffectPool snowRainEffectPool;
	Array<ParticleEffectPool.PooledEffect> effects = new Array();

	final float PIXELS_TO_METERS = 100f;

	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		sprite = new Sprite(img);
		sprite.setPosition(-sprite.getWidth() / 2, -sprite.getHeight() / 2 + 200);
		sprite2 = new Sprite(img);
		sprite2.setPosition(-sprite.getWidth() / 2 + 20,-sprite.getHeight() / 2 + 400);

		// Music
		music = Gdx.audio.newMusic(Gdx.files.internal("audio/bgm/music.mp3"));
		music.setLooping(true);

		music.play();

		// Particles
		//Set up the particle effect that will act as the pool's template
		ParticleEffect snowRainEffect = new ParticleEffect();
		snowRainEffect.load(Gdx.files.internal("graphics/particles/snow2.p"), Gdx.files.internal("graphics/pictures"));

		//If your particle effect includes additive or pre-multiplied particle emitters
		//you can turn off blend function clean-up to save a lot of draw calls, but
		//remember to switch the Batch back to GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA
		//before drawing "regular" sprites or your Stage.
		snowRainEffect.setEmittersCleanUpBlendFunction(true);

		snowRainEffectPool = new ParticleEffectPool(snowRainEffect, 1, 2);

		// Create effect:
		ParticleEffectPool.PooledEffect effect = snowRainEffectPool.obtain();
		effect.setPosition(0, Gdx.graphics.getHeight() / 2);
		effects.add(effect);

		// Physics
		world = new World(new Vector2(0, -9.8f), true);

		// Now create a BodyDefinition.  This defines the physics objects type and position in the simulation
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		// We are going to use 1 to 1 dimensions. Meaning 1 in physics engine is 1 pixel
		// Set our body to the same position as our sprite
		bodyDef.position.set(
				(sprite.getX() + sprite.getWidth() / 2) / PIXELS_TO_METERS,
				(sprite.getY() + sprite.getHeight() / 2) / PIXELS_TO_METERS
		);

		// Create a body in the world using our definition
		Body body = world.createBody(bodyDef);
		body.setUserData(sprite);
		boxes.add(body);

		BodyDef bodyDef2 = new BodyDef();
		bodyDef2.type = BodyDef.BodyType.DynamicBody;
		bodyDef2.position.set(
				(sprite2.getX() + sprite2.getWidth() / 2) / PIXELS_TO_METERS,
				(sprite2.getY() + sprite2.getHeight() / 2) / PIXELS_TO_METERS
		);

		Body body2 = world.createBody(bodyDef2);
		body2.setUserData(sprite2);
		boxes.add(body2);

		// Now define the dimensions of the physics shape
		PolygonShape shape = new PolygonShape();
		// We are a box, so this makes sense, no?
		// Basically set the physics polygon to a box with the same dimensions as our sprite
		shape.setAsBox(
				(sprite.getWidth() / 2) / PIXELS_TO_METERS,
				(sprite.getHeight() / 2) / PIXELS_TO_METERS
		);

		// FixtureDef is a confusing expression for physical properties
		// Basically this is where you, in addition to defining the shape of the body
		// you also define it's properties like density, restitution and others we will see shortly
		// If you are wondering, density and area are used to calculate over all mass
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1f;
//		fixtureDef.restitution = 0.5f;

		body.createFixture(fixtureDef);

		// Sprite2
		FixtureDef fixtureDef2 = new FixtureDef();
		fixtureDef2.shape = shape;
		fixtureDef2.density = 1f;
//		fixtureDef2.restitution = 0.5f;

		body2.createFixture(fixtureDef2);

		// Shape is the only disposable of the lot, so get rid of it
		shape.dispose();

		BodyDef bodyGround = new BodyDef();
		bodyGround.type = BodyDef.BodyType.StaticBody;
		float w = Gdx.graphics.getWidth() / PIXELS_TO_METERS;
		float h = Gdx.graphics.getHeight() / PIXELS_TO_METERS - 250 / PIXELS_TO_METERS;
		bodyGround.position.set(0,0);
		FixtureDef fixtureGround = new FixtureDef();

		EdgeShape edgeShape = new EdgeShape();
		edgeShape.set(-w/2,-h/2,w/2,-h/2);
		fixtureGround.shape = edgeShape;

		groundBody = world.createBody(bodyGround);
		groundBody.createFixture(fixtureGround);
		edgeShape.dispose();

		Gdx.input.setInputProcessor(this);

		// Create a Box2DDebugRenderer, this allows us to see the physics simulation controlling the scene
		debugRenderer = new Box2DDebugRenderer();
		font = new BitmapFont();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

		// You can savely ignore the rest of this method :)
		world.setContactListener(new ContactListener() {
			@Override
			public void beginContact (Contact contact) {
				Gdx.app.log(TAG, "begin contact");
			}

			@Override
			public void endContact (Contact contact) {
				Gdx.app.log(TAG, "end contact");
			}

			@Override
			public void preSolve (Contact contact, Manifold oldManifold) {
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
			public void postSolve (Contact contact, ContactImpulse impulse) {
				float[] ni = impulse.getNormalImpulses();
				float[] ti = impulse.getTangentImpulses();
				Gdx.app.log(TAG, "post solve, normal impulses: " + ni[0] + ", " + ni[1] + ", tangent impulses: " + ti[0] + ", " + ti[1]);
			}
		});
	}

	@Override
	public void render () {
		long start = TimeUtils.nanoTime();
		// Step the physics simulation forward at a rate of 60hz
		world.step(1f / 60f, 6, 2);
		float updateTime = (TimeUtils.nanoTime() - start) / 1000000000.0f;

		camera.update();

		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(camera.combined);

		// Scale down the sprite batches projection matrix to box2D size
		debugMatrix = batch.getProjectionMatrix().cpy().scale(PIXELS_TO_METERS, PIXELS_TO_METERS, 0);

		batch.begin();

		// Update and draw effects:
		for (int i = effects.size - 1; i >= 0; i--) {
			ParticleEffectPool.PooledEffect effect = effects.get(i);
			effect.draw(batch, Gdx.graphics.getDeltaTime());
			if (effect.isComplete()) {
				effect.free();
				effects.removeIndex(i);
			}
		}

		batch.end();

		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		batch.begin();
		//remember to switch the Batch back to GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA
		//before drawing "regular" sprites or your Stage.

		for (int i = 0; i < boxes.size(); i++) {
			Body box = boxes.get(i);
			Sprite currentSprite = ((Sprite)box.getUserData());

			currentSprite.setPosition(
					box.getPosition().x * PIXELS_TO_METERS - currentSprite.getWidth() / 2,
					box.getPosition().y * PIXELS_TO_METERS - currentSprite.getHeight() / 2
			);

			currentSprite.setRotation((float) Math.toDegrees(box.getAngle()));

			batch.draw(
					currentSprite, currentSprite.getX(), currentSprite.getY(),
					currentSprite.getOriginX(), currentSprite.getOriginY(),
					currentSprite.getWidth(), currentSprite.getHeight(),
					currentSprite.getScaleX(), currentSprite.getScaleY(),
					currentSprite.getRotation());
		}

		if(drawSprite) {
			batch.draw(sprite, sprite.getX(), sprite.getY(), sprite.getOriginX(),
					sprite.getOriginY(),
					sprite.getWidth(), sprite.getHeight(), sprite.getScaleX(), sprite.
							getScaleY(), sprite.getRotation());

			batch.draw(sprite2, sprite2.getX(), sprite2.getY(),sprite2.getOriginX(),
					sprite2.getOriginY(),
					sprite2.getWidth(),sprite2.getHeight(),sprite2.getScaleX(),sprite2.
							getScaleY(),sprite2.getRotation());
		}

		batch.end();

		batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.begin();
		font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond() + " update time: " + updateTime, 0, 20);
		batch.end();

		// Now render the physics world using our scaled down matrix
		// Note, this is strictly optional and is, as the name suggests, just for debugging purposes
		debugRenderer.render(world, debugMatrix);
	}

	@Override
	public void dispose() {
		img.dispose();
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
			// if the hit fixture's body is the ground body
			// we ignore it
			if (fixture.getBody() == groundBody) {
				Gdx.app.log(TAG, "It's the ground!");
				return true;
			}

			// if the hit point is inside the fixture of the body
			// we report it
			if (fixture.testPoint(testPoint.x, testPoint.y)) {
				Gdx.app.log(TAG, "IT'S INSIDE");
				hitBody = fixture.getBody();
				return false;
			} else {
				Gdx.app.log(TAG, "IT'S OUTSIDE");
				return true;
			}
		}
	};

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Gdx.app.log(TAG, "TOUCHDOWN");

		//body.applyForceToCenter(0f,10f,true);
		//return true;

		// translate the mouse coordinates to world coordinates
		Gdx.app.log(TAG, "testPoint (before unproject): " + testPoint);

		testPoint.set(screenX, screenY, 0);
		camera.unproject(testPoint);
		testPoint.set(testPoint.x / PIXELS_TO_METERS, testPoint.y / PIXELS_TO_METERS, 0);

		Gdx.app.log(TAG, "testPoint (after unproject): " + testPoint);

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
			def.maxForce = (10000.0f / PIXELS_TO_METERS) * hitBody.getMass();

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
			camera.unproject(testPoint.set(x, y, 0));
			mouseJoint.setTarget(target.set(testPoint.x / PIXELS_TO_METERS, testPoint.y / PIXELS_TO_METERS));
		}

		return false;
	}

	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		// if a mouse joint exists we simply destroy it
		if (mouseJoint != null) {
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
