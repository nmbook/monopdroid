package edu.rochester.nbook.monopdroid;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class DrawableRegion extends Region {
    private Drawable drawable;

    public DrawableRegion(RectF bounds, int tag, Drawable drawable) {
        super(bounds, tag);
        this.drawable = drawable.mutate();
        //Log.d("monopd", this.drawable.toString());
    }

    @Override
    public void draw(Canvas canvas) {
        Rect rect = new Rect();
        bounds.roundOut(rect);
        drawable.setBounds(rect);
        drawable.draw(canvas);
        //canvas.drawBitmap(this.bmp, null, this.getBounds(), this.imgPaint);
    }
}
