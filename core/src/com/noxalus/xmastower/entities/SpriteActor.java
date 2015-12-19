package com.noxalus.xmastower.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;

public class SpriteActor extends Group {
    public Sprite sprite;
    public Vector2 localPosition;

    public SpriteActor(Sprite sprite) {
        this.sprite = sprite;
        setBounds(getX(), getY(), sprite.getWidth(), sprite.getHeight());
        localPosition = Vector2.Zero;
        setPosition(localPosition.x, localPosition.y);
    }

    public SpriteActor(Sprite sprite, Vector2 localPosition) {
        this.sprite = sprite;
        setBounds(getX(), getY(), sprite.getWidth(), sprite.getHeight());
        this.localPosition = localPosition;
        setPosition(localPosition.x, localPosition.y);
    }

    public void draw(Batch batch, float alpha){
        batch.draw(sprite,
            getX(), getY(),
            getOriginX(), getOriginY(),
            getWidth(), getHeight(),
            getScaleX(), getScaleY(),
            getRotation()
        );
    }
}
