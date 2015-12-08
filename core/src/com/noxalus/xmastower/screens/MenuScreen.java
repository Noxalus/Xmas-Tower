package com.noxalus.xmastower.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.noxalus.xmastower.Assets;
import com.noxalus.xmastower.Config;
import com.noxalus.xmastower.XmasTower;
import com.noxalus.xmastower.entities.Gift;

import java.util.ArrayList;

public class MenuScreen implements InputProcessor, Screen {

    private static final String TAG = "MenuScreen";

    private XmasTower _game;

    private ArrayList<Gift> _gifts = new ArrayList<Gift>();

    Body _leftWallBody;
    Body _rightWallBody;
    Body _roofBody;
    Body _groundBody;

    // UI
    private Label _titleLabel;
    private Button _playButton;

    public MenuScreen(XmasTower game) {
        _game = game;

        _titleLabel = new Label("Xmas Tower", Assets.menuSkin);
        _titleLabel.setAlignment(Align.center);
        _titleLabel.setWrap(true);
        _titleLabel.setWidth(Gdx.graphics.getWidth() * 0.8f);
        _titleLabel.setPosition(
                -_titleLabel.getWidth() / 2f,
                Gdx.graphics.getHeight() / 2f - _titleLabel.getHeight() * 1.5f
        );

        _playButton = new Button(Assets.menuSkin);

        Button.ButtonStyle imageButtonStyle = new Button.ButtonStyle();
        SpriteDrawable playButtonUpSprite = new SpriteDrawable(new Sprite(Assets.playButtonUp));
        SpriteDrawable playButtonDownSprite = new SpriteDrawable(new Sprite(Assets.playButtonDown));

        _playButton.setBackground(playButtonUpSprite);

        imageButtonStyle.up = playButtonUpSprite;
        imageButtonStyle.down = playButtonDownSprite;
        imageButtonStyle.checked = playButtonUpSprite;
//        imageButtonStyle.unpressedOffsetY = -20; // to "not" center the icon
//        imageButtonStyle.unpressedOffsetX = -30; // to "not" center the icon

        _playButton.setStyle(imageButtonStyle);

//        _playButton.setPosition(
//            Gdx.graphics.getWidth() / 2f - _titleLabel.getWidth() / 2f,
//            Gdx.graphics.getHeight() - _titleLabel.getHeight() * 1.5f
//        );

        _game.Stage.addActor(_titleLabel);
        _game.Stage.addActor(_playButton);

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(_game.CustomInputProcessor);
        inputMultiplexer.addProcessor(this);

        Gdx.input.setInputProcessor(inputMultiplexer);

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
        Assets.menuMusic.play();
        addGift();
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
    }

    public void addGift() {
        Gift gift = new Gift(_game, new Vector2(0f, 0f));

        gift.initializePhysics(_game.World);
        _game.Stage.addActor(gift);
        gift.setZIndex(0);
        _gifts.add(gift);
    }

    public void draw(float delta) {
        _game.SpriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        _game.SpriteBatch.begin();
        _game.SpriteBatch.draw(Assets.playButtonUp, 0f, 0f);
//        Assets.bigFont.draw(_game.SpriteBatch, "COUCOU", Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
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

        if (screenXRatio < 0.25f && screenYRatio > 0.85f)
            launchGame();

        if (screenXRatio > 0.85f && screenYRatio > 0.85f)
            addGift();

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
