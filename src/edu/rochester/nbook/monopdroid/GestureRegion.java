package edu.rochester.nbook.monopdroid;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class GestureRegion extends Region {
    private GestureRegionListener listener;
    private boolean isPressed;
    private Paint bgPaint;

    public GestureRegion(String tag, Rect bounds, Paint paint, GestureRegionListener listener) {
        super(tag, bounds);
        this.listener = listener;
        this.bgPaint = paint;
    }

    @Override
    public void draw(Canvas canvas) {
        if (this.isPressed) {
            canvas.drawRoundRect(new RectF(this.getBounds()), 4f, 4f, this.bgPaint);
        }
    }

    public void onDown() {
        this.isPressed = true;
    }

    public void onUp() {
        this.isPressed = false;
    }

    public void invokeClick() {
        this.listener.onRegionClick(this);
    }

    public void invokeLongPress() {
        this.listener.onRegionLongPress(this);
    }
}
