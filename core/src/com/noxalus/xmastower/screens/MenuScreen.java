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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noxalus.xmastower.Assets;
import com.noxalus.xmastower.Config;
import com.noxalus.xmastower.XmasTower;
import com.noxalus.xmastower.entities.Gift;
import com.noxalus.xmastower.entities.SpriteActor;
import com.noxalus.xmastower.inputs.MenuInputProcessor;

public class MenuScreen implements InputProcessor, Screen {

    private static final String TAG = "MenuScreen";

    private XmasTower _game;

    InputMultiplexer _inputMultiplexer;

    // Physics
    Body _leftWallBody;
    Body _rightWallBody;
    Body _roofBody;
    Body _groundBody;

    // UI
    private Stage _uiStage;
    private SpriteActor _title;
    private Button _playButton;
    private Button _achievementButton;
    private Button _leaderboardButton;

    public MenuScreen(XmasTower game) {
        _game = game;

        // UI
        initializeUI();

        _inputMultiplexer = new InputMultiplexer(new MenuInputProcessor(_game), _uiStage, this);
    }

    private void initializeUI() {
        _uiStage = new Stage(new FitViewport(720, 1280));

        Sprite titleSprite = new Sprite(Assets.title);

        _title = new SpriteActor(titleSprite);

        SpriteDrawable playButtonUpSprite = new SpriteDrawable(new Sprite(Assets.playButtonUp));
        SpriteDrawable playButtonDownSprite = new SpriteDrawable(new Sprite(Assets.playButtonDown));

        SpriteDrawable achievementsButtonUpSprite = new SpriteDrawable(new Sprite(Assets.achievementsButtonUp));
        SpriteDrawable achievementsButtonDownSprite = new SpriteDrawable(new Sprite(Assets.achievementsButtonDown));

        SpriteDrawable leaderboardButtonUpSprite = new SpriteDrawable(new Sprite(Assets.leaderboardButtonUp));
        SpriteDrawable leaderboardButtonDownSprite = new SpriteDrawable(new Sprite(Assets.leaderboardButtonDown));

        Gdx.app.log(TAG, "Resolution scale ratio: " + Config.RESOLUTION_SCALE_RATIO.toString());

        _playButton = new ImageButton(playButtonUpSprite, playButtonDownSprite);
        _achievementButton = new ImageButton(achievementsButtonUpSprite, achievementsButtonDownSprite);
        _leaderboardButton = new ImageButton(leaderboardButtonUpSprite, leaderboardButtonDownSprite);

        _uiStage.setDebugAll(true);

        Gdx.app.log(TAG, "UI Stage width: " + _uiStage.getWidth());

        Table titleTable = new Table();
        titleTable.setFillParent(true);
        titleTable.setSize(_uiStage.getWidth(), _uiStage.getHeight());
        titleTable.align(Align.top | Align.center);
        titleTable.add(_title);

        Table buttonsTable = new Table();
        buttonsTable.setFillParent(true);
        buttonsTable.align(Align.center | Align.bottom);
        buttonsTable.padBottom(50);
        buttonsTable.add(_achievementButton).pad(150, 0, 0, 30);
        buttonsTable.add(_playButton);
        buttonsTable.add(_leaderboardButton).pad(150, 30, 0, 0);

        buttonsTable.sizeBy(Config.RESOLUTION_SCALE_RATIO.x, Config.RESOLUTION_SCALE_RATIO.y);

        _uiStage.addActor(titleTable);
        _uiStage.addActor(buttonsTable);

        _playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                launchGame();
            }
        });

        _achievementButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showAchievements();
            }
        });

        _leaderboardButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showLeaderboard();
            }
        });
    }

    private void initializePhysics()
    {
        float w = Gdx.graphics.getWidth() / Config.PIXELS_TO_METERS;
        float h = Gdx.graphics.getHeight() / Config.PIXELS_TO_METERS;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        bodyDef.position.set(0, 0);
        FixtureDef fixtureDef = new FixtureDef();

        EdgeShape edgeShape = new EdgeShape();

        // Left wall
        edgeShape.set(0, 0, 0, h);
        fixtureDef.shape = edgeShape;
        _leftWallBody = _game.World.createBody(bodyDef);
        _leftWallBody.createFixture(fixtureDef);

        // Right wall
        edgeShape.set(w, 0, w, h);
        fixtureDef.shape = edgeShape;
        _rightWallBody = _game.World.createBody(bodyDef);
        _rightWallBody.createFixture(fixtureDef);

        // Roof
        edgeShape.set(0, h, w, h);
        fixtureDef.shape = edgeShape;
        _roofBody = _game.World.createBody(bodyDef);
        _roofBody.createFixture(fixtureDef);

        // Ground
        edgeShape.set(0, 0, w, 0);
        fixtureDef.shape = edgeShape;
        _groundBody = _game.World.createBody(bodyDef);
        _groundBody.createFixture(fixtureDef);

        edgeShape.dispose();
    }

    @Override
    public void show() {
        _game.OnMenu = true;
        initializePhysics();
        Assets.menuMusic.play();
        addGift();

        Gdx.input.setInputProcessor(_inputMultiplexer);
    }

    private void showAchievements()
    {
        // TODO: Show Google Play achievements
    }

    private void showLeaderboard()
    {
        // TODO: Show Google Play leaderboard
    }

    private void launchGame()
    {
        Assets.menuMusic.stop();

        for (Gift g : _game.Gifts)
            _game.World.destroyBody(g.getBody());

        _game.Gifts.clear();

        _game.World.destroyBody(_leftWallBody);
        _game.World.destroyBody(_rightWallBody);
        _game.World.destroyBody(_roofBody);
        _game.World.destroyBody(_groundBody);

        _game.setScreen(_game.GameScreen);
    }

    public void addGift() {
        Vector2 giftPosition = new Vector2(
                Gdx.graphics.getWidth() / 2f,
                Gdx.graphics.getHeight() / 2f
        );

        _game.addGift(giftPosition);
    }

    public void update(float delta) {
        for (int i = 0; i < _game.Gifts.size(); i++) {
            Gift currentGift = _game.Gifts.get(i);
            currentGift.update(delta);
        }

        _uiStage.act(delta);
    }

    public void draw(float delta) {
        _game.drawGifs();
        _uiStage.draw();
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

        if (screenYRatio < 0.25f)
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
