package com.noxalus.xmastower.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noxalus.xmastower.Assets;
import com.noxalus.xmastower.Config;
import com.noxalus.xmastower.XmasTower;
import com.noxalus.xmastower.entities.Gift;
import com.noxalus.xmastower.inputs.MenuInputProcessor;

import java.util.ArrayList;

public class MenuScreen implements InputProcessor, Screen {

    private static final String TAG = "MenuScreen";

    private XmasTower _game;

    InputMultiplexer _inputMultiplexer;

    private ArrayList<Gift> _gifts = new ArrayList<Gift>();

    // Physics
    Body _leftWallBody;
    Body _rightWallBody;
    Body _roofBody;
    Body _groundBody;

    // UI
    private Button _playButton;

    public MenuScreen(XmasTower game) {
        _game = game;

        // UI
        SpriteDrawable playButtonUpSprite = new SpriteDrawable(new Sprite(Assets.playButtonUp));
        SpriteDrawable playButtonDownSprite = new SpriteDrawable(new Sprite(Assets.playButtonDown));

        _playButton = new ImageButton(playButtonUpSprite, playButtonDownSprite);
        _playButton.setPosition(
                -_playButton.getWidth() / 2f,
                -Gdx.graphics.getHeight() / 2.2f
        );

        _game.Stage.addActor(_playButton);

        _inputMultiplexer = new InputMultiplexer();
        _inputMultiplexer.addProcessor(new MenuInputProcessor(_game));
        _inputMultiplexer.addProcessor(_game.Stage);

        initializePhysics();
    }

    private void initializePhysics()
    {
        float w = Gdx.graphics.getWidth() / Config.PIXELS_TO_METERS;
        float h = Gdx.graphics.getHeight() / Config.PIXELS_TO_METERS;

        // Create ground
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        bodyDef.position.set(0, 0);
        FixtureDef fixtureDef = new FixtureDef();

        EdgeShape edgeShape = new EdgeShape();

        // Left wall
        edgeShape.set(-w / 2f, -h / 2f, -w / 2f, h / 2f);
        fixtureDef.shape = edgeShape;
        _leftWallBody = _game.World.createBody(bodyDef);
        _leftWallBody.createFixture(fixtureDef);

        // Right wall
        edgeShape.set(w / 2f, -h / 2f, w / 2f, h / 2f);
        fixtureDef.shape = edgeShape;
        _rightWallBody = _game.World.createBody(bodyDef);
        _rightWallBody.createFixture(fixtureDef);

        // Roof
        edgeShape.set(-w / 2f, h / 2f, w / 2f, h / 2f);
        fixtureDef.shape = edgeShape;
        _roofBody = _game.World.createBody(bodyDef);
        _roofBody.createFixture(fixtureDef);

        // Ground
        edgeShape.set(-w / 2f, -h / 2f, w / 2f, -h / 2f);
        fixtureDef.shape = edgeShape;
        _groundBody = _game.World.createBody(bodyDef);
        _groundBody.createFixture(fixtureDef);

        edgeShape.dispose();
    }

    @Override
    public void show() {
        _game.OnMenu = true;
        Assets.menuMusic.play();
        addGift();

        Gdx.input.setInputProcessor(_inputMultiplexer);
    }

    private void launchGame()
    {
        Assets.menuMusic.stop();
        _game.setScreen(_game.GameScreen);

        for (Gift g : _gifts)
        {
            _game.World.destroyBody(g.getBody());
        }

        _game.World.destroyBody(_leftWallBody);
        _game.World.destroyBody(_rightWallBody);
        _game.World.destroyBody(_roofBody);
        _game.World.destroyBody(_groundBody);
    }

    public void update(float delta) {
        for (int i = 0; i < _gifts.size(); i++) {
            Gift currentGift = _gifts.get(i);
            currentGift.update(delta);
        }

        if (_playButton.isChecked())
            launchGame();
    }

    public void addGift() {
        Gift gift = new Gift(new Vector2(0f, 0f));

        gift.initializePhysics(_game.World);
        _game.Stage.addActor(gift);
        gift.setZIndex(0);
        _gifts.add(gift);
    }

    public void draw(float delta) {
        _game.SpriteBatch.begin();
        _game.SpriteBatch.draw(Assets.title,
            Gdx.graphics.getWidth() / 2f - Assets.title.getWidth() / 2f,
            Gdx.graphics.getHeight() - Assets.title.getHeight()
        );
        _game.SpriteBatch.end();
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw(delta);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

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
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float screenXRatio = (float)screenX / Gdx.graphics.getWidth();
        float screenYRatio = (float)screenY / Gdx.graphics.getHeight();

//        if (screenXRatio < 0.25f && screenYRatio > 0.85f)
//            launchGame();
//
//        if (screenXRatio > 0.85f && screenYRatio > 0.85f)
//            addGift();

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
