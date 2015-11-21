package com.noxalus.xmastower;

import com.badlogic.gdx.Gdx;

public class Config {
    public static final	float PIXELS_TO_METERS = 100f;
    public static final float LINEAR_VELOCITY_THRESHOLD = 0.01f;
    public static final float GIFT_MIN_SIZE =  Gdx.graphics.getWidth() / 5f;
    public static final float GIFT_MAX_SIZE = (Gdx.graphics.getWidth() - (Gdx.graphics.getWidth() / 5f));
}
