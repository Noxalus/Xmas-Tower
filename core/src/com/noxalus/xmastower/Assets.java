package com.noxalus.xmastower;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

public class Assets {
    // Textures
    public static Texture giftTexture;
    public static Texture giftTexture2;

    // Musics
    public static Music music;

    public static Sound[] grabSounds;

    public static Texture loadTexture(String file) {
        return new Texture(Gdx.files.internal(file));
    }

    public static void load() {
        // Textures
        giftTexture = loadTexture("graphics/sprites/gift.png");
        giftTexture2 = loadTexture("graphics/sprites/gift2.png");

        // Musics
        music = Gdx.audio.newMusic(Gdx.files.internal("audio/bgm/music.mp3"));
        Assets.music.setLooping(true);

        grabSounds = new Sound[3];
        grabSounds[0] = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/grab.wav"));
        grabSounds[1] = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/grab2.wav"));
        grabSounds[2] = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/grab3.wav"));
    }
}
