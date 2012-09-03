package edu.rochester.nbook.monopdroid.board.surface;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils.TruncateAt;
import android.text.style.LeadingMarginSpan;
import android.text.Html;
import android.text.StaticLayout;
import android.text.TextPaint;

public class TextDrawable extends Drawable implements OnButtonStateChangedHandler {
    private StaticLayout layout;
    private String text;
    private TextPaint paint;
    private int enabled;
    private int disabled;
    private Alignment alignment;
    private VerticalAlignment valignment;
    private boolean singleLine;
    
    public TextDrawable(String text, int enabledColor, int disabledColor, Alignment align, VerticalAlignment valign, boolean singleLine) {
        this.enabled = enabledColor;
        this.disabled = disabledColor;
        this.text = text;
        this.paint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        this.paint.setColor(enabled);
        this.paint.setTextSize(24f);
        this.alignment = align;
        this.valignment = valign;
        this.layout = null;
        this.singleLine = singleLine;
    }

    public TextDrawable(String text, int enabledColor, int disabledColor, Alignment align, VerticalAlignment valign) {
        this(text, enabledColor, disabledColor, align, valign, false);
    }
    
    public String getText() {
        return text;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        switch (valignment) {
        case VALIGN_TOP:
            canvas.translate(this.getBounds().left, this.getBounds().top);
            break;
        case VALIGN_MIDDLE:
            canvas.translate(this.getBounds().left, this.getBounds().top + this.getBounds().height() / 2 - this.layout.getHeight() / 2);
            break;
        case VALIGN_BOTTOM:
            canvas.translate(this.getBounds().left, this.getBounds().bottom - this.layout.getHeight());
            break;
        }
        this.layout.draw(canvas);
        canvas.restore();
        // canvas.drawText(this.getTag(), this.getBounds().left,
        // this.getBounds().top + 30, this.textPaint);
    }
    
    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        updateLayout();
    }

    @Override
    public void setBounds(Rect bounds) {
        super.setBounds(bounds);
        updateLayout();
    }
    
    private void updateLayout() {
        SpannableStringBuilder formattedText = (SpannableStringBuilder) Html.fromHtml(text);
        formattedText.setSpan(new LeadingMarginSpan.Standard(0, 30), 0, formattedText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        this.layout = new StaticLayout(formattedText, 0, formattedText.length(), paint, getBounds().width(), alignment, 1f, 0f, false, TruncateAt.END, getBounds().width());
    }
    
    @Override
    public void onStateChanged(ButtonState state) {
        switch (state) {
        case NORMAL:
        case PRESSED:
        case FOCUSED:
        case CHECKED:
        case CHECKED_PRESSED:
        case CHECKED_FOCUSED:
            paint.setColor(enabled);
            break;
        case DISABLED:
        case DISABLED_FOCUSED:
        case CHECKED_DISABLED:
        case CHECKED_DISABLED_FOCUSED:
            paint.setColor(disabled);
            break;
        }
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
