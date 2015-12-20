package com.noxalus.xmastower;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public class Config {
    public static boolean ENABLE_PHYSICS_DEBUG = false;
    public static boolean ENABLE_UI_DEBUG = false;
    public static final Vector2 IDEAL_RESOLUTION = new Vector2(720, 1280);
    public static final	float PIXELS_TO_METERS = 100f;
    public static final float LINEAR_VELOCITY_THRESHOLD = 0.1f;
    public static final float GIFT_MIN_SIZE =  (Gdx.graphics.getWidth() - (Gdx.graphics.getWidth() / 1.25f));
    public static final float GIFT_MAX_SIZE = (Gdx.graphics.getWidth() - (Gdx.graphics.getWidth() / 3f));
    public static final float HEIGHT_UNIT_FACTOR = 50f;
    public static final float CAMERA_TRANSLATION_INTERPOLATION = 0.05f;
    public static final float CAMERA_INTERPOLATION_THRESHOLD = 10f;
    public static final float CAMERA_INERTIA = 0.90f;
    public static final Vector2 RESOLUTION_SCALE_RATIO = new Vector2(
        Gdx.graphics.getWidth() / IDEAL_RESOLUTION.x,
        Gdx.graphics.getHeight() / IDEAL_RESOLUTION.y
    );

    public static final String SCORE_LABEL_PLACEHOLDER = "0 cm";
}
