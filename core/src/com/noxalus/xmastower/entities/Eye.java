package com.noxalus.xmastower.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.noxalus.xmastower.Assets;
import com.noxalus.xmastower.State;

public class Eye extends StatableSpriteActor
{
    boolean _flipped;

    public Eye(Sprite sprite, boolean flipped)
    {
        super(sprite);
        _flipped = flipped;
        initialize();
    }

    public Eye(Sprite sprite, Vector2 localPosition, boolean flipped)
    {
        super(sprite, localPosition);
        _flipped = flipped;
        initialize();
    }

    private void initialize()
    {
        for (int i = 0; i < Assets.eyeRegions.length; i++)
        {
            Sprite newSprite = new Sprite(Assets.eyeRegions[i]);
            if (i != 0)
                newSprite.flip(_flipped, false);
            _sprites.add(newSprite);
        }
    }

    @Override
    public void switchState(State newState)
    {
        if (newState == State.SELECTED)
        {
            sprite = _sprites.get(1);
            setPosition(localPosition.x, localPosition.y);
        }
        else if (newState == State.FALLING)
        {
            sprite = _sprites.get(2);
            setPosition(localPosition.x, localPosition.y);
        }
        else if (newState == State.SLEEPING)
        {
            sprite = _sprites.get(4);
            setPosition(localPosition.x, localPosition.y);
        }
        else if (newState == State.COLLISIONING)
        {
            sprite = _sprites.get(3);
            float lag = _flipped ? -1f : 3f;
            setPosition(localPosition.x + lag, localPosition.y + 3f);
        }
        else if (newState == State.SICK)
        {
            sprite = _sprites.get(6);
            float lag = _flipped ? -1f : 3f;
            setPosition(localPosition.x + lag, localPosition.y + 3f);
        }
        else
        {
            sprite = _sprites.get(0);
            setPosition(localPosition.x, localPosition.y);
        }

        super.switchState(newState);
    }
}
