package edu.rochester.nbook.monopdroid;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class GestureRegion extends Region {
    private GestureRegionListener listener;
    private boolean isPressed;
    private Paint bgPaint;
    private boolean enabled;

    public GestureRegion(RectF bounds, int tag, Paint paint, GestureRegionListener listener) {
        super(bounds, tag);
        this.listener = listener;
        this.bgPaint = paint;
        this.enabled = true;
    }

    @Override
    public void draw(Canvas canvas) {
        if (this.isPressed) {
            canvas.drawRoundRect(new RectF(this.getBounds()), 4f, 4f, this.bgPaint);
        }
    }

    public void onDown() {
        if (this.enabled) {
            this.isPressed = true;
        }
    }

    public void onUp() {
        if (this.enabled) {
            this.isPressed = false;
        }
    }

    public void invokeClick() {
        if (this.enabled) {
            this.listener.onRegionClick(this);
        }
    }

    public void invokeLongPress() {
        if (this.enabled) {
            this.listener.onRegionLongPress(this);
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.isPressed = enabled && this.isPressed;
    }
}
