package edu.rochester.nbook.monopdroid;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout.Alignment;

public class TextRegion extends Region {
    private StaticLayout layout;

    public TextRegion(String text, Rect bounds, Paint textPaint, Alignment align) {
        super(text, bounds);
        this.layout = new StaticLayout(text, new TextPaint(textPaint), this.getBounds().right
                        - this.getBounds().left, align, 1f, 0f, false);
        super.bounds.top = super.bounds.top - this.layout.getHeight() / 2;
        super.bounds.bottom = super.bounds.top + this.layout.getHeight();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.translate(this.getBounds().left, this.getBounds().top);
        this.layout.draw(canvas);
        canvas.restore();
        // canvas.drawText(this.getTag(), this.getBounds().left,
        // this.getBounds().top + 30, this.textPaint);
    }
}
