package edu.rochester.nbook.monopdroid;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;


public class DrawableRegion extends Region {
    private Paint imgPaint;
    private Bitmap bmp;

    public DrawableRegion(String tag, Rect bounds, Bitmap bitmap, Paint imgPaint) {
        super(tag, bounds);
        this.bmp = bitmap;
        this.imgPaint = imgPaint;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(this.bmp, null, this.getBounds(), this.imgPaint);
    }
}
