package edu.rochester.nbook.monopdroid;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;

public class NinePatchDrawableRegion extends Region {
    private NinePatchDrawable npd;

    public NinePatchDrawableRegion(String tag, Rect bounds, NinePatchDrawable npd) {
        super(tag, bounds);
        this.npd = npd;
        this.npd.setBounds(super.bounds);
    }

    @Override
    public void draw(Canvas canvas) {
        this.npd.draw(canvas);
    }
}
