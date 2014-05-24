package com.natembook.monopdroid.board.surface;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

public class RectDrawable extends Drawable implements OnButtonStateChangedHandler {
    private Drawable draw = null;
    private Drawable pressedDraw = null;
    private Paint borderPaint = null;
    private ButtonState state = ButtonState.NORMAL;
    
    public RectDrawable(int color, int borderColor, int borderThickness) {
        this(new ColorDrawable(color), borderColor, borderThickness);
    }
    
    public RectDrawable(int color, int selectedColor, int borderColor, int borderThickness) {
        this(new ColorDrawable(color), borderColor, borderThickness);
        pressedDraw = new ColorDrawable(selectedColor);
    }
    
    public RectDrawable(Drawable inner, int borderColor, int borderThickness) {
        draw = inner;
        if (borderColor != 0) {
            borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            borderPaint.setColor(borderColor);
            borderPaint.setStyle(Style.STROKE);
            borderPaint.setStrokeWidth(borderThickness);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        int saveCount = canvas.save();
        canvas.clipRect(bounds);
        if (pressedDraw != null &&
                (state == ButtonState.PRESSED || state == ButtonState.CHECKED_PRESSED ||
                 state == ButtonState.FOCUSED || state == ButtonState.CHECKED_FOCUSED)) {
            pressedDraw.draw(canvas);
        } else {
            draw.draw(canvas);
        }
        canvas.restoreToCount(saveCount);
        if (borderPaint != null) {
            canvas.drawRect(bounds, borderPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        draw.setAlpha(alpha);
        if (pressedDraw != null) {
            pressedDraw.setAlpha(alpha);
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        draw.setColorFilter(cf);
        if (pressedDraw != null) {
            pressedDraw.setColorFilter(cf);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
    
    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        draw.setBounds(left, top, right, bottom);
        if (pressedDraw != null) {
            pressedDraw.setBounds(left, top, right, bottom);
        }
    }
    
    @Override
    public void setBounds(Rect bounds) {
        super.setBounds(bounds);
        draw.setBounds(bounds);
        if (pressedDraw != null) {
            pressedDraw.setBounds(bounds);
        }
    }

    @Override
    public void onStateChanged(ButtonState state) {
        this.state = state;
    }
}
