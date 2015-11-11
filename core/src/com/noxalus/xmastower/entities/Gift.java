package com.noxalus.xmastower.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.noxalus.xmastower.Assets;
import com.noxalus.xmastower.Config;
import com.noxalus.xmastower.XmasTower;

public class Gift {
    Sprite _sprite;
    Body _body;
    boolean _isPlaced = false;
    boolean _isSelected = false;
    boolean _isMovable = true;
    private XmasTower _game;

    public Gift(XmasTower game, Vector2 position) {
        _game = game;
        _sprite = new Sprite(Assets.giftTexture);
        _sprite.setPosition(position.x, position.y);

        Gdx.app.log("GIFT", "Set initial gift position to: " + position.x + ", " + position.y);
    }

    public void initializePhysics(World world) {
        // Now create a BodyDefinition.  This defines the physics objects type and position in the simulation
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        // We are going to use 1 to 1 dimensions. Meaning 1 in physics engine is 1 pixel
        // Set our body to the same position as our sprite
        bodyDef.position.set(
                (_sprite.getX() + _sprite.getWidth() / 2) / Config.PIXELS_TO_METERS,
                (_sprite.getY() + _sprite.getHeight() / 2) / Config.PIXELS_TO_METERS
        );

        // Create a body in the world using our definition
        _body = world.createBody(bodyDef);
        _body.setUserData(this);

        // Now define the dimensions of the physics shape
        PolygonShape shape = new PolygonShape();
        // We are a box, so this makes sense, no?
        // Basically set the physics polygon to a box with the same dimensions as our sprite
        shape.setAsBox(
                (((_sprite.getWidth() / 2) - 11) * _sprite.getScaleX()) / Config.PIXELS_TO_METERS,
                ((275f / 2) * _sprite.getScaleY()) / Config.PIXELS_TO_METERS,
                new Vector2(0, -0.5f),
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

    public void update(float delta) {
//        Gdx.app.log("GIFT", "Linear velocity: " + _body.getLinearVelocity());
//        Gdx.app.log("GIFT", "Sprite position: " + _sprite.getX() + ", " + _sprite.getY());

        // Outside of the scene?
        if (_sprite.getX() < -Gdx.graphics.getWidth() / 2f - _sprite.getWidth() ||
                _sprite.getX() > Gdx.graphics.getWidth() / 2 ||
                _sprite.getY() < -Gdx.graphics.getHeight() ||
                _sprite.getY() > Gdx.graphics.getHeight() + _sprite.getHeight())
        {
            _game.reset();
        }
        else if (!_isPlaced && !_isMovable && _body.getLinearVelocity().x == 0.f && _body.getLinearVelocity().y == 0.f) {
            Gdx.app.log("GIFT", "HAS STOP TO MOVE");
            _isPlaced = true;

            Gdx.app.log("GIFT", "Sprite position: " + _sprite.getX() + ", " + _sprite.getY());
            _game.moveCamera(new Vector2(0f, _sprite.getHeight()));
            _game.addGift();
        }
    }

    public void draw(float delta, Batch batch) {
        _sprite.setPosition(
                _body.getPosition().x * Config.PIXELS_TO_METERS - _sprite.getWidth() / 2,
                _body.getPosition().y * Config.PIXELS_TO_METERS - _sprite.getHeight() / 2
        );

        _sprite.setRotation((float) Math.toDegrees(_body.getAngle()));

        batch.draw(
                _sprite, _sprite.getX(), _sprite.getY(),
                _sprite.getOriginX(), _sprite.getOriginY(),
                _sprite.getWidth(), _sprite.getHeight(),
                _sprite.getScaleX(), _sprite.getScaleY(),
                _sprite.getRotation()
        );
    }

    public boolean isMovable()
    {
        return _isMovable;
    }

    public void isSelected(boolean value) {
        if (_isSelected && !value)
            _isMovable = false;

        _isSelected = value;
    }

    public Body getBody()
    {
        return _body;
    }
}
