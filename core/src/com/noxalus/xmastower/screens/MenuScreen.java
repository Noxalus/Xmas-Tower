package com.noxalus.xmastower.screens;

import com.badlogic.gdx.Screen;
import com.noxalus.xmastower.XmasTower;

public class MenuScreen implements Screen {

    private static final String TAG = "MenuScreen";
    
    private XmasTower _game;

    public MenuScreen(XmasTower game)
    {
        _game = game;

        // TODO: Instanciate Label and ImageButton
    }

    @Override
    public void show() {
        // TODO: Play menu music
    }

    public void update(float delta) {

    }

    public void draw(float delta) {

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
}
