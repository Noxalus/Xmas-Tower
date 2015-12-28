package com.noxalus.xmastower;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;

public class Assets {
    // Texture atlas
    public static TextureAtlas atlas;

    // Texture regions
    public static TextureRegion[] normalBoxRegions;
    public static TextureRegion[] ribbonRegions;
    public static TextureRegion[] eyeRegions;
    public static TextureRegion[] mouthRegions;

    public static Texture title;
    public static Texture groundTexture;
    public static Texture barleySugarTexture;
    public static Texture whitePixel;
    public static Texture[] backgrounds;

    // Fonts
    public static BitmapFont normalFont;
    public static BitmapFont mediumFont;
    public static BitmapFont bigFont;

    // UI
    public static Skin menuSkin;
    public static Texture playButtonUp;
    public static Texture playButtonDown;
    public static Texture achievementsButtonUp;
    public static Texture achievementsButtonDown;
    public static Texture leaderboardButtonUp;
    public static Texture leaderboardButtonDown;
    public static Texture backButtonUp;
    public static Texture backButtonDown;
    public static Texture zoomOutButtonUp;
    public static Texture zoomOutButtonDown;
    public static Texture shareButtonUp;
    public static Texture shareButtonDown;

    // Musics
    public static Music menuMusic;
    public static Music gameMusicIntro;
    public static Music gameMusicLoop;

    // SFX
    public static Sound[] grabSounds;
    public static Sound[] ouchSounds;
    public static Sound[] fallSounds;

    public static void load() {
        // Texture atlas
        atlas = new TextureAtlas("graphics/sprites/atlas.pack");

        // Texture regions
        // TODO: Reorganize insertion order according to ribbon color instead of gift box color
        normalBoxRegions = new TextureRegion[30];
        normalBoxRegions[0] = atlas.findRegion("giftbox_red_pink");
        normalBoxRegions[1] = atlas.findRegion("giftbox_yellow_pink");
        normalBoxRegions[2] = atlas.findRegion("giftbox_green_pink");
        normalBoxRegions[3] = atlas.findRegion("giftbox_white_pink");
        normalBoxRegions[4] = atlas.findRegion("giftbox_blue_pink");

        normalBoxRegions[5] = atlas.findRegion("giftbox_pink_yellow");
        normalBoxRegions[6] = atlas.findRegion("giftbox_red_yellow");
        normalBoxRegions[7] = atlas.findRegion("giftbox_green_yellow");
        normalBoxRegions[8] = atlas.findRegion("giftbox_white_yellow");
        normalBoxRegions[9] = atlas.findRegion("giftbox_blue_yellow");

        normalBoxRegions[10] = atlas.findRegion("giftbox_pink_red");
        normalBoxRegions[11] = atlas.findRegion("giftbox_yellow_red");
        normalBoxRegions[12] = atlas.findRegion("giftbox_green_red");
        normalBoxRegions[13] = atlas.findRegion("giftbox_white_red");
        normalBoxRegions[14] = atlas.findRegion("giftbox_blue_red");

        normalBoxRegions[15] = atlas.findRegion("giftbox_pink_green");
        normalBoxRegions[16] = atlas.findRegion("giftbox_red_green");
        normalBoxRegions[17] = atlas.findRegion("giftbox_yellow_green");
        normalBoxRegions[18] = atlas.findRegion("giftbox_white_green");
        normalBoxRegions[19] = atlas.findRegion("giftbox_blue_green");

        normalBoxRegions[20] = atlas.findRegion("giftbox_pink_white");
        normalBoxRegions[21] = atlas.findRegion("giftbox_red_white");
        normalBoxRegions[22] = atlas.findRegion("giftbox_yellow_white");
        normalBoxRegions[23] = atlas.findRegion("giftbox_green_white");
        normalBoxRegions[24] = atlas.findRegion("giftbox_blue_white");

        normalBoxRegions[25] = atlas.findRegion("giftbox_pink_blue");
        normalBoxRegions[26] = atlas.findRegion("giftbox_red_blue");
        normalBoxRegions[27] = atlas.findRegion("giftbox_yellow_blue");
        normalBoxRegions[28] = atlas.findRegion("giftbox_green_blue");
        normalBoxRegions[29] = atlas.findRegion("giftbox_white_blue");

        ribbonRegions = new TextureRegion[6];
        ribbonRegions[0] = atlas.findRegion("ribbon1");
        ribbonRegions[1] = atlas.findRegion("ribbon2");
        ribbonRegions[2] = atlas.findRegion("ribbon3");
        ribbonRegions[3] = atlas.findRegion("ribbon4");
        ribbonRegions[4] = atlas.findRegion("ribbon5");
        ribbonRegions[5] = atlas.findRegion("ribbon6");

        eyeRegions = new TextureRegion[8];
        eyeRegions[0] = atlas.findRegion("eye1");
        eyeRegions[1] = atlas.findRegion("eye2");
        eyeRegions[2] = atlas.findRegion("eye3");
        eyeRegions[3] = atlas.findRegion("eye4");
        eyeRegions[4] = atlas.findRegion("eye5");
        eyeRegions[5] = atlas.findRegion("eye6");
        eyeRegions[6] = atlas.findRegion("eye7");
        eyeRegions[7] = atlas.findRegion("eye8");

        mouthRegions = new TextureRegion[7];
        mouthRegions[0] = atlas.findRegion("mouth1");
        mouthRegions[1] = atlas.findRegion("mouth2");
        mouthRegions[2] = atlas.findRegion("mouth3");
        mouthRegions[3] = atlas.findRegion("mouth4");
        mouthRegions[4] = atlas.findRegion("mouth5");
        mouthRegions[5] = atlas.findRegion("mouth6");
        mouthRegions[6] = atlas.findRegion("mouth7");

        title = new Texture(Gdx.files.internal("graphics/pictures/title.png"));
        whitePixel = new Texture(Gdx.files.internal("graphics/pictures/white.png"));
        groundTexture = new Texture(Gdx.files.internal("graphics/sprites/ground.png"));
        barleySugarTexture = new Texture(Gdx.files.internal("graphics/sprites/barleysugar.png"));

        backgrounds = new Texture[1];
        backgrounds[0] = new Texture(Gdx.files.internal("graphics/pictures/background1.png"));

        // Fonts
        normalFont = new BitmapFont(
            Gdx.files.internal("fonts/normalfont.fnt"),
            Gdx.files.internal("fonts/normalfont_00.png"),
            false
        );

        mediumFont = new BitmapFont(
            Gdx.files.internal("fonts/mediumfont.fnt"),
            Gdx.files.internal("fonts/mediumfont_00.png"),
            false
        );

        bigFont = new BitmapFont(
            Gdx.files.internal("fonts/bigfont.fnt"),
            Gdx.files.internal("fonts/bigfont_00.png"),
            false
        );

        // Skins
        menuSkin = new Skin(Gdx.files.internal("ui/menu.json"));
        playButtonUp = new Texture(Gdx.files.internal("graphics/ui/playbuttonup.png"));
        playButtonDown = new Texture(Gdx.files.internal("graphics/ui/playbuttondown.png"));

        achievementsButtonUp = new Texture(Gdx.files.internal("graphics/ui/achievementsbuttonup.png"));
        achievementsButtonDown = new Texture(Gdx.files.internal("graphics/ui/achievementsbuttondown.png"));

        leaderboardButtonUp = new Texture(Gdx.files.internal("graphics/ui/leaderboardbuttonup.png"));
        leaderboardButtonDown = new Texture(Gdx.files.internal("graphics/ui/leaderboardbuttondown.png"));

        backButtonUp = new Texture(Gdx.files.internal("graphics/ui/backbuttonup.png"));
        backButtonDown = new Texture(Gdx.files.internal("graphics/ui/backbuttondown.png"));

        zoomOutButtonUp = new Texture(Gdx.files.internal("graphics/ui/zoomoutbuttonup.png"));
        zoomOutButtonDown = new Texture(Gdx.files.internal("graphics/ui/zoomoutbuttondown.png"));

        shareButtonUp = new Texture(Gdx.files.internal("graphics/ui/sharebuttonup.png"));
        shareButtonDown = new Texture(Gdx.files.internal("graphics/ui/sharebuttondown.png"));

        // Musics
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/bgm/menu.ogg"));
        gameMusicIntro = Gdx.audio.newMusic(Gdx.files.internal("audio/bgm/intro.ogg"));
        gameMusicLoop = Gdx.audio.newMusic(Gdx.files.internal("audio/bgm/music.ogg"));

        gameMusicIntro.setOnCompletionListener(new Music.OnCompletionListener() {
            @Override
            public void onCompletion(Music music) {
            Assets.gameMusicLoop.play();
            }
        });

        menuMusic.setLooping(true);
        gameMusicLoop.setLooping(true);

        // SFX
        grabSounds = new Sound[3];
        grabSounds[0] = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/grab1.wav"));
        grabSounds[1] = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/grab2.wav"));
        grabSounds[2] = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/grab3.wav"));

        ouchSounds = new Sound[8];
        ouchSounds[0] = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/ouch1.wav"));
        ouchSounds[1] = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/ouch2.wav"));
        ouchSounds[2] = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/ouch3.wav"));
        ouchSounds[3] = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/ouch4.wav"));
        ouchSounds[4] = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/ouch5.wav"));
        ouchSounds[5] = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/ouch6.wav"));
        ouchSounds[6] = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/ouch7.wav"));
        ouchSounds[7] = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/ouch8.wav"));

        fallSounds = new Sound[1];
        fallSounds[0] = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/fall1.wav"));
    }
}
