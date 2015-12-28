package com.noxalus.xmastower.inputs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.noxalus.xmastower.Assets;
import com.noxalus.xmastower.Config;
import com.noxalus.xmastower.XmasTower;
import com.noxalus.xmastower.entities.Gift;
import com.noxalus.xmastower.screens.GameScreen;

public class GameInputProcessor implements InputProcessor{

    private final String TAG = "GameInputProcessor";
    private XmasTower _game;
    private GameScreen _gameScreen;
    private Sound _currentPlayedSound;
    private int _oldY;

    public GameInputProcessor(XmasTower game, GameScreen gameScreen)
    {
        _game = game;
        _gameScreen = gameScreen;
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

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        if (pointer > 0 || _game.MouseJoint != null)
            return false;

        // translate the mouse coordinates to World coordinates
        _game.FixtureTestPoint.set(x, y, 0);
        _game.Camera.unproject(_game.FixtureTestPoint);
        _game.FixtureTestPoint.set(_game.FixtureTestPoint.x / Config.PIXELS_TO_METERS, _game.FixtureTestPoint.y / Config.PIXELS_TO_METERS, 0);

        // ask the World which bodies are within the given
        // bounding box around the mouse pointer
        _game.HitBody = null;
        float value = 0.1f;
        _game.World.QueryAABB(
                _game.FixtureQueryCallback,
                _game.FixtureTestPoint.x - value,
                _game.FixtureTestPoint.y - value,
                _game.FixtureTestPoint.x + value,
                _game.FixtureTestPoint.y + value
        );

        // if we hit something we create a new mouse joint
        // and attach it to the hit body.
        if (_game.MouseJoint == null && _game.HitBody != null) {
            if (_game.SoundsEnabled) {
                _currentPlayedSound = Assets.grabSounds[MathUtils.random(0, Assets.grabSounds.length - 1)];
                _currentPlayedSound.play();
            }

            MouseJointDef def = new MouseJointDef();
            def.bodyA = _game.HitBody;
            def.bodyB = _game.HitBody;
            def.collideConnected = true;
            def.target.set(_game.FixtureTestPoint.x, _game.FixtureTestPoint.y);
            def.maxForce = (10000.0f / Config.PIXELS_TO_METERS) * _game.HitBody.getMass();

            _game.MouseJoint = (MouseJoint) _game.World.createJoint(def);
            _game.HitBody.setAwake(true);
        }

        _oldY = y;

        return false;
    }


    @Override
    public boolean touchDragged (int x, int y, int pointer) {
        if (pointer > 0)
            return false;

        if (_gameScreen.GameIsFinished) {
            int deltaY = y - _oldY;

            _gameScreen.CameraSpeedY = deltaY;
        }
        else {
            if (_game.MouseJoint != null) {
                _game.Camera.unproject(_game.FixtureTestPoint.set(x, y, 0));
                _game.MouseJoint.setTarget(_game.MouseJointTarget.set(
                        _game.FixtureTestPoint.x / Config.PIXELS_TO_METERS,
                        _game.FixtureTestPoint.y / Config.PIXELS_TO_METERS
                ));
            }
        }

        _oldY = y;

        return false;
    }

    @Override
    public boolean touchUp (int x, int y, int pointer, int button) {

        if (_currentPlayedSound != null)
        {
            _currentPlayedSound.stop();
            _currentPlayedSound = null;
        }

        if (_game.MouseJoint != null && _game.HitBody != null) {
            ((Gift)(_game.HitBody.getUserData())).isSelected(false);
            _game.World.destroyJoint(_game.MouseJoint);
            _game.MouseJoint = null;
            _game.HitBody = null;
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
