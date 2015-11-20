package com.noxalus.xmastower.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.noxalus.xmastower.Assets;
import com.noxalus.xmastower.Config;
import com.noxalus.xmastower.XmasTower;


public class Gift extends Group {
    Group _group;
    Sprite _boxSprite;
    Actor _ribon;
    Actor _leftEye;
    Actor _rightEye;
    Actor _mouth;
    Body _body;
    Vector2 _ribonLocalPosition;
    Vector2 _leftEyeLocalPosition;
    Vector2 _rightEyeLocalPosition;
    Vector2 _mouthLocalPosition;

    boolean _isPlaced = false;
    boolean _isSelected = false;
    boolean _isMovable = true;
    private XmasTower _game;

    public Gift(XmasTower game, Vector2 position) {
        Gdx.app.log("GIFT", "Gift initial position: " + position.toString());

        _game = game;

        _boxSprite = new Sprite(Assets.normalBoxRegions[MathUtils.random(0, Assets.normalBoxRegions.length - 1)]);

        setBounds(getX(), getY(), _boxSprite.getWidth(), _boxSprite.getHeight());

        _ribon = new Actor(){
            public Sprite sprite = new Sprite(Assets.ribonRegions[0]);

            public void draw(Batch batch, float alpha){
                batch.draw(sprite,
                    getX(), getY(),
                    getOriginX(), getOriginY(),
                    getWidth(), getHeight(),
                    getScaleX(), getScaleY(),
                    getRotation()
                );
            }
        };
        _ribon.setBounds(0, 0, Assets.ribonRegions[0].getRegionWidth(), Assets.ribonRegions[0].getRegionHeight());
        _ribonLocalPosition = new Vector2(0f, 28.f);
        _ribon.setPosition(_ribonLocalPosition.x, _ribonLocalPosition.y);

        _leftEye = new Actor(){
            public Sprite sprite = new Sprite(Assets.eyeRegions[0]);

            public void draw(Batch batch, float alpha){
                batch.draw(sprite,
                        getX(), getY(),
                        getOriginX(), getOriginY(),
                        getWidth(), getHeight(),
                        getScaleX(), getScaleY(),
                        getRotation()
                );
            }
        };

        _leftEye.setBounds(0, 0, Assets.eyeRegions[0].getRegionWidth(), Assets.eyeRegions[0].getRegionHeight());
        _leftEyeLocalPosition = new Vector2(12f, 15f);
        _leftEye.setPosition(_leftEyeLocalPosition.x, _leftEyeLocalPosition.y);
        _leftEye.setScale(0.75f, 0.75f);

        _rightEye = new Actor(){
            public Sprite sprite = new Sprite(Assets.eyeRegions[0]);

            public void draw(Batch batch, float alpha){
                batch.draw(sprite,
                        getX(), getY(),
                        getOriginX(), getOriginY(),
                        getWidth(), getHeight(),
                        getScaleX(), getScaleY(),
                        getRotation()
                );
            }
        };

        _rightEye.setBounds(0, 0, Assets.eyeRegions[0].getRegionWidth(), Assets.eyeRegions[0].getRegionHeight());
        _rightEyeLocalPosition = new Vector2(40f, 15f);
        _rightEye.setPosition(_rightEyeLocalPosition.x, _rightEyeLocalPosition.y);
        _rightEye.setScale(0.75f, 0.75f);

        _mouth = new Actor(){
            public Sprite sprite = new Sprite(Assets.mouthRegions[0]);

            public void draw(Batch batch, float alpha){
                batch.draw(sprite,
                        getX(), getY(),
                        getOriginX(), getOriginY(),
                        getWidth(), getHeight(),
                        getScaleX(), getScaleY(),
                        getRotation()
                );
            }
        };

        _mouth.setBounds(0, 0, Assets.mouthRegions[0].getRegionWidth(), Assets.mouthRegions[0].getRegionHeight());
        _mouthLocalPosition = new Vector2(26f, 8f);
        _mouth.setPosition(_mouthLocalPosition.x, _mouthLocalPosition.y);
        _mouth.setScale(0.75f, 0.75f);

        float scale = MathUtils.random(2f, 8f);

        _group = new Group();
        _group.addActor(this);
        _group.addActor(_leftEye);
        _group.addActor(_rightEye);
        _group.addActor(_mouth);
        _group.addActor(_ribon);

        _game.stage.addActor(_group);

        _group.setPosition(
                position.x - (_boxSprite.getWidth() / 2f) * scale,
                position.y - _boxSprite.getHeight() * scale
        );
        _group.setScale(scale, scale);

//        Gdx.app.log("GIFT", "Set initial gift position to: " + _group.getX() + ", " + _group.getY());
    }

    public void initializePhysics(World world) {
        // Now create a BodyDefinition.  This defines the physics objects type and position in the simulation
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        bodyDef.position.set(
            (_group.getX() + (_boxSprite.getWidth() / 2f) * _group.getScaleX()) / Config.PIXELS_TO_METERS,
            (_group.getY() + (_boxSprite.getHeight() / 2f) * _group.getScaleY()) / Config.PIXELS_TO_METERS
        );

        _body = world.createBody(bodyDef);
        _body.setUserData(this);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
                (((_boxSprite.getWidth() / 2)) * _group.getScaleX()) / Config.PIXELS_TO_METERS,
                ((_boxSprite.getHeight() / 2) * _group.getScaleY()) / Config.PIXELS_TO_METERS,
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

        _body.createFixture(fixtureDef);

        _body.setAwake(false);

        // Shape is the only disposable of the lot, so get rid of it
        shape.dispose();
    }

    public void act(float delta) {

//        Gdx.app.log("GIFT", "Linear velocity: " + _body.getLinearVelocity());

        if (_game.GameWillReset)
            return;

        if (_isSelected)
            Gdx.app.log("GIFT", "Sprite position: " + _group.getX() + ", " + _group.getY());


        float linearVelocityThreshold = 0.01f;
        // Outside of the scene?
        if (_group.getX() < -Gdx.graphics.getWidth() / 2f - _group.getWidth() ||
                _group.getX() > Gdx.graphics.getWidth() / 2 ||
                _group.getY() < -Gdx.graphics.getHeight())
        {
            _game.gameFinished();
        }
        else if (!_isPlaced && !_isMovable &&
                _body.getLinearVelocity().x < linearVelocityThreshold &&
                _body.getLinearVelocity().x > -linearVelocityThreshold &&
                _body.getLinearVelocity().y < linearVelocityThreshold &&
                _body.getLinearVelocity().y > -linearVelocityThreshold) {
            Gdx.app.log("GIFT", "HAS STOP TO MOVE");
            _isPlaced = true;

//            Gdx.app.log("GIFT", "Sprite position: " + _boxSprite.getX() + ", " + _boxSprite.getY());

            Vector3 screenCoordinates = _game._camera.project(new Vector3(_group.getX(), _group.getY(), 0.f));
            if (screenCoordinates.y > Gdx.graphics.getWidth() / 2.f)
                _game.translateCamera(new Vector2(0f, _group.getHeight()));

            if (_game._score < _group.getY() + 925)
                _game._score = (int) _group.getY() + 925;

            _game.addGift();
        }

        _group.setPosition(
                (_body.getPosition().x * Config.PIXELS_TO_METERS) - _boxSprite.getWidth() / 2,
                (_body.getPosition().y * Config.PIXELS_TO_METERS) - _boxSprite.getHeight() / 2
        );

        _group.setOrigin(
                _boxSprite.getOriginX(),
                _boxSprite.getOriginY()
        );

        _group.setRotation((float) Math.toDegrees(_body.getAngle()));
    }

    public void draw(Batch batch, float alpha) {
        batch.draw(_boxSprite,
                getX(), getY(),
                getOriginX(), getOriginY(),
                getWidth(), getHeight(),
                getScaleX(), getScaleY(),
                getRotation()
        );
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
//        if (value)
//            _boxSprite.setTexture(Assets.giftTexture2);
//        else
//            _boxSprite.setTexture(Assets.giftTexture);

        _isSelected = value;
    }

    public Body getBody()
    {
        return _body;
    }
}
