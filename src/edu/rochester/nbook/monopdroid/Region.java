package edu.rochester.nbook.monopdroid;

import android.graphics.Canvas;
import android.graphics.RectF;

public abstract class Region {
    protected RectF bounds;
    protected int tag;

    public Region(RectF bounds, int tag) {
        this.bounds = bounds;
    }

    public RectF getBounds() {
        return this.bounds;
    }
    
    public int getTag() {
        return tag;
    }

    public abstract void draw(Canvas canvas);
}
