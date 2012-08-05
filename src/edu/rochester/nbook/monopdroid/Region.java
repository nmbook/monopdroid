package edu.rochester.nbook.monopdroid;

import android.graphics.Canvas;
import android.graphics.Rect;

public abstract class Region {
    protected String tag;
    protected Rect bounds;

    public Region(String tag, Rect bounds) {
        this.tag = tag;
        this.bounds = bounds;
    }

    public String getTag() {
        return this.tag;
    }

    public Rect getBounds() {
        return this.bounds;
    }

    public abstract void draw(Canvas canvas);
}
