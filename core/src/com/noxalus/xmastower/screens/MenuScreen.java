package com.noxalus.xmastower.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.noxalus.xmastower.Assets;
import com.noxalus.xmastower.XmasTower;
import com.noxalus.xmastower.entities.Gift;

import java.util.ArrayList;

public class MenuScreen implements InputProcessor, Screen {

    private static final String TAG = "MenuScreen";

    private XmasTower _game;

    private ArrayList<Gift> _gifts = new ArrayList<Gift>();

    private Label _titleLabel;
    private Button _playButton;

    public MenuScreen(XmasTower game) {
        _game = game;

        _titleLabel = new Label("Xmas Tower", Assets.menuSkin);
        _titleLabel.setAlignment(Align.center);
        _titleLabel.setWrap(true);
        _titleLabel.setWidth(Gdx.graphics.getWidth() * 0.8f);
        _titleLabel.setPosition(
                Gdx.graphics.getWidth() / 2f - _titleLabel.getWidth() / 2f,
                Gdx.graphics.getHeight() - _titleLabel.getHeight() * 1.5f
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

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void show() {
        Assets.menuMusic.play();
    }

    public void update(float delta) {

    }

    public void draw(float delta) {
        _game.SpriteBatch.begin();
        _game.SpriteBatch.end();

        _game.SpriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        _game.SpriteBatch.begin();
//        _game.SpriteBatch.draw(Assets.playButtonUp, 0f, 0f);
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
        Assets.menuMusic.stop();
        _game.setScreen(_game.GameScreen);
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
