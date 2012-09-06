package edu.rochester.nbook.monopdroid.board.surface;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class RotateDrawable extends Drawable {
    private Drawable toRotate;
    private float degrees;
    
    public RotateDrawable(Drawable toRotate, float degrees) {
        this.toRotate = toRotate;
        this.degrees = degrees;
    }

    @Override
    public void draw(Canvas canvas) {
        int saveCount = canvas.save();
        canvas.rotate(degrees, toRotate.getBounds().exactCenterX(), toRotate.getBounds().exactCenterY());
        toRotate.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public void setAlpha(int alpha) {
        toRotate.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        toRotate.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return toRotate.getOpacity();
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        toRotate.setBounds(left, top, right, bottom);
    }
    
    @Override
    public void setBounds(Rect bounds) {
        toRotate.setBounds(bounds);
    }
}
