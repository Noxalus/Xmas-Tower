package com.noxalus.xmastower.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

enum State{
    IDLE,
    SELECTED,
    FALLING,
    COLLISIONING,
    SLEEPING
};

public class StatableSpriteActor extends SpriteActor
{
    ArrayList<Sprite> _sprites = new ArrayList<Sprite>();
    State _currentState = State.IDLE;

    public StatableSpriteActor(Sprite sprite)
    {
        super(sprite);
    }

    public StatableSpriteActor(Sprite sprite, Vector2 localPosition)
    {
        super(sprite, localPosition);
    }

    public void switchState(State newState)
    {
        _currentState = newState;
        setBounds(getX(), getY(), sprite.getWidth(), sprite.getHeight());
    }
}
