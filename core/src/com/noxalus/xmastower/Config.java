package com.noxalus.xmastower;

import com.badlogic.gdx.Gdx;

public class Config {
    public static final	float PIXELS_TO_METERS = 100f;
    public static final float LINEAR_VELOCITY_THRESHOLD = 0.01f;
    public static final float GIFT_MIN_SIZE =  (Gdx.graphics.getWidth() - (Gdx.graphics.getWidth() / 1.25f));
    public static final float GIFT_MAX_SIZE = (Gdx.graphics.getWidth() - (Gdx.graphics.getWidth() / 3f));
    public static final float HEIGHT_UNIT_FACTOR = 50f;

    public static final String SCORE_LABEL_PLACEHOLDER = "0 cm";
}
