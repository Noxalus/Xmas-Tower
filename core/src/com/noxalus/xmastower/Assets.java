package com.noxalus.xmastower;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;

public class Assets {
    // Textures
    public static Texture giftTexture;

    // Musics
    public static Music music;

    public static Texture loadTexture(String file) {
        return new Texture(Gdx.files.internal(file));
    }

    public static void load() {
        // Textures
        giftTexture = loadTexture("graphics/sprites/gift.png");

        // Musics
        music = Gdx.audio.newMusic(Gdx.files.internal("audio/bgm/music.mp3"));
        Assets.music.setLooping(true);

    }
}
