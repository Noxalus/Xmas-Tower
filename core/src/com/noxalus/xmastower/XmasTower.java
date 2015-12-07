package com.noxalus.xmastower;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.noxalus.xmastower.screens.MenuScreen;
import com.noxalus.xmastower.screens.GameScreen;

public class XmasTower extends Game {

	private static final String TAG = "XmasTower";

	public SpriteBatch SpriteBatch;

	public MenuScreen MenuScreen;
	public GameScreen GameScreen;

    // Particles
    ParticleEffectPool _snowRainEffectPool;
    Array<ParticleEffectPool.PooledEffect> _effects = new Array();

	@Override
	public void create () {
		SpriteBatch = new SpriteBatch();
		Assets.load();

        MenuScreen = new MenuScreen(this);
		GameScreen = new GameScreen(this);

		setScreen(MenuScreen);

        initializeParticles();
    }

    private void initializeParticles() {

        //Set up the particle effect that will act as the pool's template
        ParticleEffect snowRainEffect = new ParticleEffect();
        snowRainEffect.load(
            Gdx.files.internal("graphics/particles/snow2.p"),
            Gdx.files.internal("graphics/pictures")
        );

        //If your particle effect includes additive or pre-multiplied particle emitters
        //you can turn off blend function clean-up to save a lot of draw calls, but
        //remember to switch the Batch back to GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA
        //before drawing "regular" sprites or your Stage.
        snowRainEffect.setEmittersCleanUpBlendFunction(true);

        _snowRainEffectPool = new ParticleEffectPool(snowRainEffect, 1, 2);

        // Create effect:
        ParticleEffectPool.PooledEffect effect = _snowRainEffectPool.obtain();
        effect.setPosition(Gdx.graphics.getWidth() / 2.f, Gdx.graphics.getHeight() + 200.f);
        _effects.add(effect);
    }

    public void update() {

    }

    public void draw() {
        SpriteBatch.begin();

        // Update and draw _effects:
        for (int i = _effects.size - 1; i >= 0; i--) {
            ParticleEffectPool.PooledEffect effect = _effects.get(i);
            effect.draw(SpriteBatch, Gdx.graphics.getDeltaTime());
            if (effect.isComplete()) {
                effect.free();
                _effects.removeIndex(i);
            }
        }

        SpriteBatch.end();
    }

    @Override
    public void render () {
        Gdx.gl.glClearColor(0.13f, 0.5f, 0.8f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update();
        draw();

        super.render();
    }
}
