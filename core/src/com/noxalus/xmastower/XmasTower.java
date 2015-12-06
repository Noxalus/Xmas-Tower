package com.noxalus.xmastower;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.noxalus.xmastower.screens.MenuScreen;
import com.noxalus.xmastower.screens.GameScreen;

public class XmasTower extends Game {

	private static final String TAG = "XmasTower";

	public SpriteBatch SpriteBatch;

	public MenuScreen MenuScreen;
	public GameScreen GameScreen;

	@Override
	public void create () {
		SpriteBatch = new SpriteBatch();
		Assets.load();

		GameScreen = new GameScreen(this);
		MenuScreen = new MenuScreen(this);

		setScreen(GameScreen);
	}

	public void resize(int width, int height) {
	}
}
