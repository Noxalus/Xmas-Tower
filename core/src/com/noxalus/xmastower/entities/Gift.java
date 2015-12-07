package com.noxalus.xmastower.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.noxalus.xmastower.Assets;
import com.noxalus.xmastower.Config;
import com.noxalus.xmastower.State;
import com.noxalus.xmastower.XmasTower;
import com.noxalus.xmastower.screens.GameScreen;


public class Gift extends Group {
    private XmasTower _game;

    SpriteActor _box;
    SpriteActor _ribbon;
    Eye _leftEye;
    Eye _rightEye;
    Mouth _mouth;

    Body _body;

    boolean _isPlaced = false;
    boolean _isSelected = false;
    boolean _isMovable = true;

    public Gift(XmasTower game, Vector2 position) {
        _game = game;

        initializeActors();

        float minScale = Config.GIFT_MIN_SIZE / _box.sprite.getWidth();
        float maxScale = Config.GIFT_MAX_SIZE / _box.sprite.getWidth();
        float scale = MathUtils.random(minScale, maxScale);

        addActor(_box);
        addActor(_leftEye);
        addActor(_rightEye);
        addActor(_mouth);
        addActor(_ribbon);

        setPosition(
            position.x - (_box.getWidth() / 2f) * scale,
            position.y - _box.getHeight() * scale
        );
        setScale(scale, scale);
    }

    public void initializeActors()
    {
        _box = new SpriteActor(new Sprite(Assets.normalBoxRegions[MathUtils.random(0, Assets.normalBoxRegions.length - 1)]));
        _ribbon = new SpriteActor(new Sprite(Assets.ribbonRegions[0]), new Vector2(0f, 28.f));
        _leftEye = new Eye(new Sprite(Assets.eyeRegions[0]), new Vector2(12f, 15f), false);
        _leftEye.setScale(0.75f, 0.75f);
        _rightEye = new Eye(new Sprite(Assets.eyeRegions[0]), new Vector2(40f, 15f), true);
        _rightEye.setScale(0.75f, 0.75f);
        _mouth = new Mouth(new Sprite(Assets.mouthRegions[0]), new Vector2(26f, 8f));
        _mouth.setScale(0.75f, 0.75f);
    }

    public void initializePhysics(World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        bodyDef.position.set(
            (getX() + (_box.sprite.getWidth() / 2f) * getScaleX()) / Config.PIXELS_TO_METERS,
            (getY() + (_box.sprite.getHeight() / 2f) * getScaleY()) / Config.PIXELS_TO_METERS
        );

        _body = world.createBody(bodyDef);
        _body.setUserData(this);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
            (((_box.sprite.getWidth() / 2f) - 1f) * getScaleX()) / Config.PIXELS_TO_METERS,
            ((_box.sprite.getHeight() / 2f) * getScaleY()) / Config.PIXELS_TO_METERS,
            new Vector2(0, 0),
            0f
        );

        // FixtureDef is a confusing expression for physical properties
        // Basically this is where you, in addition to defining the shape of the body
        // you also define it's properties like density, restitution and others we will see shortly
        // If you are wondering, density and area are used to calculate over all mass
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
//		fixtureDef.restitution = 0.5f;

        _body.setLinearDamping(1.f);
        _body.createFixture(fixtureDef);
        _body.setAwake(false);

        shape.dispose();
    }

    public void update(float delta) {
//        Gdx.app.log("GIFT", "Linear velocity: " + _body.getLinearVelocity());

        if (_game.GameScreen.GameWillReset)
            return;

//        if (_isSelected)
//            Gdx.app.log("GIFT", "Sprite position: " + getX() + ", " + getY());

        // Outside of the scene?
        if (getX() < -Gdx.graphics.getWidth() / 2f - _box.sprite.getWidth() ||
            getX() > Gdx.graphics.getWidth() / 2 ||
            getY() < -Gdx.graphics.getHeight())
        {
            _game.GameScreen.gameFinished();
        }
        else if (!_isPlaced && !_isMovable &&
                _body.getLinearVelocity().x < Config.LINEAR_VELOCITY_THRESHOLD &&
                _body.getLinearVelocity().x > -Config.LINEAR_VELOCITY_THRESHOLD &&
                _body.getLinearVelocity().y < Config.LINEAR_VELOCITY_THRESHOLD &&
                _body.getLinearVelocity().y > -Config.LINEAR_VELOCITY_THRESHOLD) {
            Gdx.app.log("GIFT", "HAS STOP TO MOVE");
            _isPlaced = true;

            switchState(State.SLEEPING);

            Vector3 screenCoordinates = _game.Camera.project(new Vector3(getX(), getY(), 0.f));
            float limitThreshold = Gdx.graphics.getWidth() / 1.5f;

            if (screenCoordinates.y > limitThreshold)
                _game.GameScreen.translateCamera(new Vector2(0f, Gdx.graphics.getWidth() / 2f));

            _game.GameScreen._score++;
            _game.GameScreen.addGift();
        }

        setPosition((_body.getPosition().x * Config.PIXELS_TO_METERS) - _box.sprite.getWidth() / 2f, (_body.getPosition().y * Config.PIXELS_TO_METERS) - _box.sprite.getHeight() / 2f);
        setOrigin(_box.sprite.getOriginX(), _box.sprite.getOriginY());
        setRotation((float) Math.toDegrees(_body.getAngle()));
    }

    public boolean isMovable()
    {
        return _isMovable;
    }

    public void isMovable(boolean value)
    {
        _isMovable = value;
    }

    public void isSelected(boolean value) {
        if (value)
        {
            switchState(State.SELECTED);
        }
        else if (_isMovable)
        {
            switchState(State.FALLING);
        }

        _isSelected = value;
    }

    public Body getBody()
    {
        return _body;
    }

    public void switchState(State newState)
    {
        _leftEye.switchState(newState);
        _rightEye.switchState(newState);
        _mouth.switchState(newState);
    }
}
