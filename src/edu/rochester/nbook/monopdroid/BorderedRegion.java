package edu.rochester.nbook.monopdroid;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class BorderedRegion extends Region {
    private Paint bgPaint;
    private Paint borderPaint;

    public BorderedRegion(RectF bounds, int tag, Paint bgPaint, Paint borderPaint) {
        super(bounds, tag);
        this.bgPaint = new Paint(bgPaint);
        this.borderPaint = borderPaint;
    }
    
    @Override
    public void draw(Canvas canvas) {
        canvas.drawRect(bounds, bgPaint);
        if (borderPaint != null) {
            canvas.drawRect(bounds, borderPaint);
        }
    }

}
