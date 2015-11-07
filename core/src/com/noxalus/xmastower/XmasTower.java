package com.noxalus.xmastower;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
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
import com.badlogic.gdx.physics.box2d.World;

public class XmasTower extends ApplicationAdapter implements InputProcessor {
	SpriteBatch batch;
	Sprite sprite, sprite2;
	Texture img;
	Music music;
	World world;
	Body body, body2;
	Body bodyEdgeScreen;
	Box2DDebugRenderer debugRenderer;
	Matrix4 debugMatrix;
	OrthographicCamera camera;
	BitmapFont font;

	float torque = 0.0f;
	boolean drawSprite = true;

	final float PIXELS_TO_METERS = 100f;

	@Override
	public void create () {
		batch = new SpriteBatch();

		img = new Texture("badlogic.jpg");
		sprite = new Sprite(img);
		sprite.setPosition(-sprite.getWidth()/2,-sprite.getHeight()/2 + 200);
		sprite2 = new Sprite(img);
		sprite2.setPosition(-sprite.getWidth()/2 + 20,-sprite.getHeight()/2 + 400);

		music = Gdx.audio.newMusic(Gdx.files.internal("audio/bgm/music.mp3"));
		music.setLooping(true);

		music.play();

		world = new World(new Vector2(0, -1f), true);

		// Now create a BodyDefinition.  This defines the physics objects type and position in the simulation
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		// We are going to use 1 to 1 dimensions.  Meaning 1 in physics engine is 1 pixel
		// Set our body to the same position as our sprite
		bodyDef.position.set(
				(sprite.getX() + sprite.getWidth() / 2) / PIXELS_TO_METERS,
				(sprite.getY() + sprite.getHeight() / 2) / PIXELS_TO_METERS
		);

		// Create a body in the world using our definition
		body = world.createBody(bodyDef);

		// Sprite2's physics body
		BodyDef bodyDef2 = new BodyDef();
		bodyDef2.type = BodyDef.BodyType.DynamicBody;
		bodyDef2.position.set(
				(sprite2.getX() + sprite2.getWidth() / 2) / PIXELS_TO_METERS,
				(sprite2.getY() + sprite2.getHeight() / 2) / PIXELS_TO_METERS);

		body2 = world.createBody(bodyDef2);

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
		fixtureDef.density = 0.1f;
		fixtureDef.restitution = 0.5f;

		body.createFixture(fixtureDef);

		// Sprite2
		FixtureDef fixtureDef2 = new FixtureDef();
		fixtureDef2.shape = shape;
		fixtureDef2.density = 0.1f;
		fixtureDef2.restitution = 0.5f;

		body2.createFixture(fixtureDef2);

		// Shape is the only disposable of the lot, so get rid of it
		shape.dispose();

		BodyDef bodyGround = new BodyDef();
		bodyGround.type = BodyDef.BodyType.StaticBody;
		float w = Gdx.graphics.getWidth()/PIXELS_TO_METERS;
		// Set the height to just 50 pixels above the bottom of the screen so we can see the edge in the
		// debug renderer
		float h = Gdx.graphics.getHeight()/PIXELS_TO_METERS- 50/PIXELS_TO_METERS;
		//bodyDef2.position.set(0,
//                h-10/PIXELS_TO_METERS);
		bodyGround.position.set(0,0);
		FixtureDef fixturGround = new FixtureDef();

		EdgeShape edgeShape = new EdgeShape();
		edgeShape.set(-w/2,-h/2,w/2,-h/2);
		fixturGround.shape = edgeShape;

		bodyEdgeScreen = world.createBody(bodyGround);
		bodyEdgeScreen.createFixture(fixturGround);
		edgeShape.dispose();

		Gdx.input.setInputProcessor(this);

		// Create a Box2DDebugRenderer, this allows us to see the physics simulation controlling the scene
		debugRenderer = new Box2DDebugRenderer();
		font = new BitmapFont();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

		world.setContactListener(new ContactListener() {
			@Override
			public void beginContact(Contact contact) {
				// Check to see if the collision is between the second sprite and the bottom of the screen
				// If so apply a random amount of upward force to both objects... just because
				if((contact.getFixtureA().getBody() == bodyEdgeScreen &&
						contact.getFixtureB().getBody() == body2)
						||
						(contact.getFixtureA().getBody() == body2 &&
								contact.getFixtureB().getBody() == bodyEdgeScreen)) {

					body.applyForceToCenter(0,MathUtils.random(20,50),true);
					body2.applyForceToCenter(0, MathUtils.random(20, 50), true);
				}
			}

			@Override
			public void endContact(Contact contact) {
			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
			}
		});
	}

	@Override
	public void render () {
		camera.update();
		// Step the physics simulation forward at a rate of 60hz
		world.step(1f/60f, 6, 2);

		// Apply torque to the physics body. At start this is 0 and will do nothing. Controlled with[] keys
		// Torque is applied per frame instead of just once
		body.applyTorque(torque,true);

		// Set the sprite's position from the updated physics body location
		sprite.setPosition(
				(body.getPosition().x * PIXELS_TO_METERS) - sprite.getWidth() / 2,
				(body.getPosition().y * PIXELS_TO_METERS) -sprite.getHeight() / 2
		);
		sprite.setRotation((float) Math.toDegrees(body.getAngle()));

		sprite2.setPosition(
				(body2.getPosition().x * PIXELS_TO_METERS) - sprite2.getWidth() / 2 ,
				(body2.getPosition().y * PIXELS_TO_METERS) -sprite2.getHeight() / 2
		);
		sprite2.setRotation((float)Math.toDegrees(body2.getAngle()));

		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(camera.combined);

		// Scale down the sprite batches projection matrix to box2D size
		debugMatrix = batch.getProjectionMatrix().cpy().scale(PIXELS_TO_METERS, PIXELS_TO_METERS, 0);

		batch.begin();

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

		font.draw(batch,
				"Restitution: " + body.getFixtureList().first().getRestitution(),
				-Gdx.graphics.getWidth() / 2,
				Gdx.graphics.getHeight() / 2 );

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

	// On touch we apply force from the direction of the users touch.
	// This could result in the object "spinning"
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		//body.applyForce(1f, 1f, screenX, screenY, true);
		body.applyForceToCenter(0f,10f,true);
		//body.applyForceToCenter(1.f, 1.f, true);
		//body.applyTorque(0.4f,true);
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
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
