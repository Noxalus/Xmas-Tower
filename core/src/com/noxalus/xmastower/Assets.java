package com.noxalus.xmastower;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Assets {
    // Textures
    public static Texture giftTexture;
    public static Texture giftTexture2;

    // Texture atlas
    public static TextureAtlas atlas;

    // Texture regions
    public static TextureRegion[] normalBoxRegions;
    public static TextureRegion[] ribonRegions;

    // Musics
    public static Music music;

    // SFX
    public static Sound[] grabSounds;
    public static Sound[] ouchSounds;

    public static Texture loadTexture(String file) {
        return new Texture(Gdx.files.internal(file));
    }

    public static void load() {
        // Textures
        giftTexture = loadTexture("graphics/sprites/gift.png");
        giftTexture2 = loadTexture("graphics/sprites/gift2.png");

        // Texture atlas
        atlas = new TextureAtlas("graphics/sprites/Xmas-Tower.pack");

        // Texture regions
        normalBoxRegions = new TextureRegion[4];
        normalBoxRegions[0] = atlas.findRegion("giftbox1");
        normalBoxRegions[1] = atlas.findRegion("giftbox2");
        normalBoxRegions[2] = atlas.findRegion("giftbox3");
        normalBoxRegions[3] = atlas.findRegion("giftbox4");

        ribonRegions = new TextureRegion[1];
        ribonRegions[0] = atlas.findRegion("ribon1");

        // Musics
        music = Gdx.audio.newMusic(Gdx.files.internal("audio/bgm/music.mp3"));
        Assets.music.setLooping(true);

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
    }
}
