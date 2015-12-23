package com.noxalus.xmastower.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Timer;
import com.noxalus.xmastower.Assets;
import com.noxalus.xmastower.Config;
import com.noxalus.xmastower.State;

public class Gift extends Group {
    SpriteActor _box;
    SpriteActor _ribbon;
    Eye _leftEye;
    Eye _rightEye;
    Mouth _mouth;

    Body _body;

    State _currentState = State.IDLE;

    boolean _isPlaced = false;
    boolean _isSelected = false;
    boolean _isMovable = true;
    boolean _isFalling = false;
    boolean _isSick = false;
    boolean _isAngry = false;
    boolean _isPouting = false;
    boolean _isHurt = false;

    Sound _fallSound;

    public SpriteActor getRibbon() {
        return  _ribbon;
    }

    public Gift(Vector2 position) {
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
        int boxId = MathUtils.random(0, Assets.normalBoxRegions.length - 1);
        int ribbonId = boxId / 5; // Link ribbon and box colors

        _box = new SpriteActor(new Sprite(Assets.normalBoxRegions[boxId]));
        _ribbon = new SpriteActor(new Sprite(Assets.ribbonRegions[ribbonId]), new Vector2(0f, 28.f));
        _ribbon.setScale(0.525f, 0.525f);
        _ribbon.setVisible(false);
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
        fixtureDef.friction = 0.4f;

        _body.setLinearDamping(1.f);
        _body.createFixture(fixtureDef);
        _body.setAwake(false);
        _body.setSleepingAllowed(true);

        shape.dispose();
    }

    public void update(float delta) {
//        if (_isFalling && _body.getLinearVelocity().y < -10 && _fallSound == null)
//        {
//            _fallSound = Assets.fallSounds[MathUtils.random(Assets.fallSounds.length - 1)];
//            _fallSound.setLooping(0, false);
//            _fallSound.play();
//        }

        if (!_isSick && Math.abs(getRotation()) > 360)
        {
            switchState(State.SICK);
            _isSick = true;
        }

        if (_isPlaced && Math.abs(getRotation()) > 5) {
            switchState(State.POUTING);
            _isPouting = true;
        } else if (_isPlaced && _isPouting && Math.abs(getRotation()) < 5) {
            switchState(State.SLEEPING);
            _isPouting = false;
        }

//        if (_isSelected) {
//            Gdx.app.log("GIFT", "Sprite position: " + getX() + ", " + getY());
//        }

        setPosition(
                (_body.getPosition().x * Config.PIXELS_TO_METERS) - _box.sprite.getWidth() / 2f,
                (_body.getPosition().y * Config.PIXELS_TO_METERS) - _box.sprite.getHeight() / 2f
        );
        setOrigin(_box.sprite.getOriginX(), _box.sprite.getOriginY());
        setRotation((float) Math.toDegrees(_body.getAngle()));
    }

    public void isHurt(boolean value) {
        if (value) {
            switchState(State.HURT);

            if (!_isAngry) {
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        switchState(State.ANGRY);
                        _isAngry = true;
                    }
                }, 1);
            }
        }

        _isHurt = value;
    }

    // LocalPosition relative to center
    public Vector2 getWorldPosition(Vector2 localPosition) {
        float cos = (float)Math.cos(Math.toRadians(getRotation()));
        float sin = (float)Math.sin(Math.toRadians(getRotation()));

        float originX = getCenter().x;
        float originY = getCenter().y;

        float x1 = getCenter().x + (localPosition.x * getScaleX());
        float y1 = getCenter().y + (localPosition.y * getScaleY());

        float x2 = (x1 - originX) * cos - (y1 - originY) * sin + originX;
        float y2 = (x1 - originX) * sin + (y1 - originY) * cos + originY;

        return new Vector2(x2, y2);
    }

    public float getHighestPosition() {
        Vector2 leftBottomCornerPosition = getWorldPosition(new Vector2(
            (-_box.sprite.getWidth() / 2f),
            (-_box.sprite.getHeight() / 2f))
        );
        Vector2 leftUpCornerPosition = getWorldPosition(new Vector2(
            (-_box.sprite.getWidth() / 2f),
            (_box.sprite.getHeight() / 2f))
        );
        Vector2 rightBottomCornerPosition = getWorldPosition(new Vector2(
            (_box.sprite.getWidth() / 2f),
            (-_box.sprite.getHeight() / 2f))
        );
        Vector2 rightUpCornerPosition = getWorldPosition(new Vector2(
            (_box.sprite.getWidth() / 2f),
            (_box.sprite.getHeight() / 2f))
        );

        return Math.max(
            Math.max(
                leftBottomCornerPosition.y,
                leftUpCornerPosition.y
            ),
            Math.max(
                rightBottomCornerPosition.y,
                rightUpCornerPosition.y
            )
        );
    }

    public Vector2 getCenter() {
        return new Vector2(
            getX() + (_box.sprite.getWidth() / 2f),
            getY() + (_box.sprite.getHeight() / 2f)
        );
    }

    public boolean isSick() {
        return _isSick;
    }

    public boolean isMovable()
    {
        return _isMovable;
    }

    public void isMovable(boolean value)
    {
        _isMovable = value;
    }

    public boolean isPlaced()
    {
        return _isPlaced;
    }

    public void isPlaced(boolean value)
    {
        if (value)
            _isFalling = false;

        _isPlaced = value;
    }

    public void isSelected(boolean value) {
        if (value)
        {
            _isFalling = false;
            switchState(State.SELECTED);
        }
        else if (_isMovable)
        {
            _isFalling = true;
            switchState(State.FALLING);
        }

        _isSelected = value;
    }

    public Body getBody()
    {
        return _body;
    }

    public SpriteActor getBox() {
        return _box;
    }

    public void switchState(State newState)
    {
        if (_isSick || _isAngry)
            return;

        _leftEye.switchState(newState);
        _rightEye.switchState(newState);
        _mouth.switchState(newState);

        _currentState = newState;
    }

    public void drawRibbon(Batch batch)
    {
        this.applyTransform(batch, computeTransform());
        _ribbon.setVisible(true);
        _ribbon.draw(batch, 1);
        _ribbon.setVisible(false);
        super.resetTransform(batch);
    }
}
