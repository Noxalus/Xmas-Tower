package com.noxalus.xmastower.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.noxalus.xmastower.Assets;

public class Mouth extends StatableSpriteActor
{
    public Mouth(Sprite sprite)
    {
        super(sprite);
        initialize();
    }

    public Mouth(Sprite sprite, Vector2 localPosition)
    {
        super(sprite, localPosition);
        initialize();
    }

    private void initialize()
    {
        for (int i = 0; i < Assets.mouthRegions.length; i++)
        {
            Sprite newSprite = new Sprite(Assets.mouthRegions[i]);
            _sprites.add(newSprite);
        }
    }

    @Override
    public void switchState(State newState)
    {

        if (newState == State.SELECTED)
        {
            sprite = _sprites.get(1);
            setPosition(27f, 8f);
        }
        else if (newState == State.COLLISIONING)
        {
            sprite = _sprites.get(2);
            setPosition(25f, 8f);
        }
        else if (newState == State.SLEEPING)
        {
            sprite = _sprites.get(0);
            setPosition(26f, 8f);
        }
        else
        {
            sprite = _sprites.get(0);
            setPosition(26f, 8f);
        }

        super.switchState(newState);
    }
}
