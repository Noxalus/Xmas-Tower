package com.noxalus.xmastower.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.noxalus.xmastower.Assets;
import com.noxalus.xmastower.Config;
import com.noxalus.xmastower.XmasTower;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;

public class Gift extends Actor {
    Sprite _boxSprite;
    Sprite _ribonSprite;
    Actor _ribon;
    TextureRegion _boxTextureRegion;
    TextureRegion _ribonTextureRegion;
    Body _body;
    Vector2 _ribonLocalPosition;
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



//        position.x +=  -_boxSprite.getWidth() / 2f;
//        position.y +=  -_boxSprite.getHeight() / 2f;

//        _ribonSprite.setOrigin(_boxSprite.getOriginX(), _boxSprite.getOriginY());

        float scale = MathUtils.random(0.5f, 1.2f);
        scale = 5f;
        //_boxSprite.setScale(scale, scale);
        //_boxSprite.setScale(0.5f, 0.5f);
//        _boxSprite.setScale(scale, scale);
//        _ribonSprite.setScale(scale, scale);
//        _boxSprite.setScale(MathUtils.random(0.5f, 1.25f), MathUtils.random(0.5f, 1.25f));

        _ribonLocalPosition = new Vector2(0f * getScaleX(), (_boxSprite.getHeight() / 2f) * getScaleY());
        _ribon.setPosition(_ribonLocalPosition.x, _ribonLocalPosition.y);

        Group group = new Group();
        group.addActor(this);
        group.addActor(_ribon);

//        group.addAction(parallel(moveTo(200, 0, 5), rotateBy(90f, 5f)));

        _game.stage.addActor(group);

        this.setPosition(
                position.x - _boxSprite.getWidth() / 2f,
                position.y - _boxSprite.getHeight()
        );
        this.setScale(scale, scale);

        Gdx.app.log("GIFT", "Set initial gift position to: " + position.x + ", " + position.y);
    }

    public void initializePhysics(World world) {
        // Now create a BodyDefinition.  This defines the physics objects type and position in the simulation
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        bodyDef.position.set(
            (_boxSprite.getX() + _boxSprite.getWidth() / 2) / Config.PIXELS_TO_METERS,
            (_boxSprite.getY() + _boxSprite.getHeight() / 2) / Config.PIXELS_TO_METERS
        );

        _body = world.createBody(bodyDef);
        _body.setUserData(this);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
                (((_boxSprite.getWidth() / 2)) * getScaleX()) / Config.PIXELS_TO_METERS,
                ((_boxSprite.getHeight() / 2) * getScaleY()) / Config.PIXELS_TO_METERS,
                new Vector2(0, 0),
                0f
        );

//        shape.setAsBox(
//                (((_boxSprite.getWidth() / 2) - 11) * _boxSprite.getScaleX()) / Config.PIXELS_TO_METERS,
//                ((275f / 2) * _boxSprite.getScaleY()) / Config.PIXELS_TO_METERS,
//                new Vector2(0 * _boxSprite.getScaleX(), -0.5f * _boxSprite.getScaleY()),
//                0f
//        );

        // FixtureDef is a confusing expression for physical properties
        // Basically this is where you, in addition to defining the shape of the body
        // you also define it's properties like density, restitution and others we will see shortly
        // If you are wondering, density and area are used to calculate over all mass
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 10f;
//		fixtureDef.restitution = 0.5f;

        _body.createFixture(fixtureDef);

        _body.setAwake(false);

        // Shape is the only disposable of the lot, so get rid of it
        shape.dispose();
    }

    public void update(float delta) {

//        Gdx.app.log("GIFT", "Linear velocity: " + _body.getLinearVelocity());

        if (_game.GameWillReset)
            return;

        if (_isSelected)
        Gdx.app.log("GIFT", "Sprite position: " + _boxSprite.getX() + ", " + _boxSprite.getY());

        float linearVelocityThreshold = 0.01f;
        // Outside of the scene?
        if (_boxSprite.getX() < -Gdx.graphics.getWidth() / 2f - _boxSprite.getWidth() ||
                _boxSprite.getX() > Gdx.graphics.getWidth() / 2 ||
                _boxSprite.getY() < -Gdx.graphics.getHeight())
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

            Gdx.app.log("GIFT", "Sprite position: " + _boxSprite.getX() + ", " + _boxSprite.getY());

            Vector3 screenCoordinates = _game._camera.project(new Vector3(_boxSprite.getX(), _boxSprite.getY(), 0.f));
            if (screenCoordinates.y > Gdx.graphics.getWidth() / 2.f)
                _game.translateCamera(new Vector2(0f, _boxSprite.getHeight() * getScaleY()));

            if (_game._score < _boxSprite.getY() + 925)
                _game._score = (int) _boxSprite.getY() + 925;

            _game.addGift();
        }
    }

    public void draw(Batch batch, float alpha) {
        this.setPosition(
                _body.getPosition().x * Config.PIXELS_TO_METERS - _boxSprite.getWidth() / 2,
                _body.getPosition().y * Config.PIXELS_TO_METERS - _boxSprite.getHeight() / 2
        );
        this.setRotation((float) Math.toDegrees(_body.getAngle()));

        batch.draw(_boxSprite,
            getX(), getY(),
            getOriginX(), getOriginY(),
            getWidth(), getHeight(),
            getScaleX(), getScaleY(),
            getRotation()
        );
    }

    public void draw(float delta, Batch batch) {
        _boxSprite.setPosition(
                _body.getPosition().x * Config.PIXELS_TO_METERS - _boxSprite.getWidth() / 2,
                _body.getPosition().y * Config.PIXELS_TO_METERS - _boxSprite.getHeight() / 2
        );
        _boxSprite.setRotation((float) Math.toDegrees(_body.getAngle()));

        float radianAngle = (_body.getAngle() + (float)Math.PI / 2f) % (float)Math.PI;
        Vector2 direction = new Vector2((float)Math.cos(radianAngle), (float)Math.sin(radianAngle));

        Vector2 ribonLocalPosition = new Vector2(
            _boxSprite.getX() + (_ribonLocalPosition.x * direction.x),
            _boxSprite.getY() + (_ribonLocalPosition.y * direction.y)
        );

        Gdx.app.log("GIFT", "Box position: " + _boxSprite.getX() + ", " + _boxSprite.getY());
        Gdx.app.log("GIFT", "Box angle: " + _body.getAngle());
        Gdx.app.log("GIFT", "Box direction: " + direction.toString());
        Gdx.app.log("GIFT", "Ribon local position (before): " + ribonLocalPosition.toString());
//        ribonLocalPosition = _body.getWorldPoint(ribonLocalPosition);
        Gdx.app.log("GIFT", "Ribon local position (after): " + ribonLocalPosition.toString());
//        ribonLocalPosition.x /= Config.PIXELS_TO_METERS;
//        ribonLocalPosition.y /= Config.PIXELS_TO_METERS;
//        b2Rot rot( body->GetAngle() );
//        ribonLocalPosition.x = (_body.getAngle() * ribonLocalPosition.x) + _boxSprite.getX();
//        ribonLocalPosition.y = (_body.getAngle() * ribonLocalPosition.y) + _boxSprite.getY();
//        pos = b2Mul(rot, pos) + body->GetPosition();
//        ang += -body->GetAngle();

        _ribonSprite.setPosition(ribonLocalPosition.x, ribonLocalPosition.y);

        _ribonSprite.setRotation(_boxSprite.getRotation());
//        _ribonSprite.setOrigin(_ribonSprite.getWidth() / 2, _ribonSprite.getHeight() / 2);

        batch.draw(_boxSprite,
            _boxSprite.getX(), _boxSprite.getY(),
            _boxSprite.getOriginX(), _boxSprite.getOriginY(),
            _boxSprite.getWidth(), _boxSprite.getHeight(),
            _boxSprite.getScaleX(), _boxSprite.getScaleY(),
            _boxSprite.getRotation()
        );

        batch.draw(_ribonSprite,
            _ribonSprite.getX(), _ribonSprite.getY(),
            _ribonSprite.getOriginX(), _ribonSprite.getOriginY(),
            _ribonSprite.getWidth(), _ribonSprite.getHeight(),
            _ribonSprite.getScaleX(), _ribonSprite.getScaleY(),
            _ribonSprite.getRotation()
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
