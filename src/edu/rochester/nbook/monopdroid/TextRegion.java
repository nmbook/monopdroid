package edu.rochester.nbook.monopdroid;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;

public class TextRegion extends Region {
    private StaticLayout layout;
    private String text;

    public TextRegion(RectF rectF, int tag, String text, Paint textPaint, Alignment align) {
        super(rectF, tag);
        this.text = text;
        this.layout = new StaticLayout(text, new TextPaint(textPaint), (int)(this.getBounds().right - this.getBounds().left),
                align, 1f, 0f, false);
        super.bounds.top = super.bounds.top - this.layout.getHeight() / 2;
        super.bounds.bottom = super.bounds.top + this.layout.getHeight();
    }
    
    public String getText() {
        return text;
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
