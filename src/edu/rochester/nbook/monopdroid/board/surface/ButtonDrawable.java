package edu.rochester.nbook.monopdroid.board.surface;

import edu.rochester.nbook.monopdroid.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class ButtonDrawable extends Drawable implements OnButtonStateChangedHandler {
    protected Drawable[] states;
    protected int state;
    
    public ButtonDrawable(Context context) {
        this(context, 5);
    }
    
    protected ButtonDrawable(Context context, int stateCount) {
        states = new Drawable[stateCount];
        if (stateCount == 5) {
            states[0] = context.getResources().getDrawable(R.drawable.btn_default_holo_dark);
            states[1] = context.getResources().getDrawable(R.drawable.btn_default_pressed_holo_dark);
            states[2] = context.getResources().getDrawable(R.drawable.btn_default_focused_holo_dark);
            states[3] = context.getResources().getDrawable(R.drawable.btn_default_disabled_holo_dark);
            states[4] = context.getResources().getDrawable(R.drawable.btn_default_disabled_focused_holo_dark);
        }
        state = 0;
    }
    
    @Override
    public void onStateChanged(ButtonState state) {
        switch (state) {
        default:
        case NORMAL:
            this.state = 0;
            break;
        case PRESSED:
            this.state = 1;
            break;
        case FOCUSED:
            this.state = 2;
            break;
        case DISABLED:
            this.state = 3;
            break;
        case DISABLED_FOCUSED:
            this.state = 4;
            break;
        case CHECKED:
            this.state = 5;
            break;
        case CHECKED_PRESSED:
            this.state = 6;
            break;
        case CHECKED_FOCUSED:
            this.state = 7;
            break;
        case CHECKED_DISABLED:
            this.state = 8;
            break;
        case CHECKED_DISABLED_FOCUSED:
            this.state = 9;
            break;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        states[state].draw(canvas);
    }

    @Override
    public void setAlpha(int alpha) {
        for (Drawable state : states) {
            state.setAlpha(alpha);
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        for (Drawable state : states) {
            state.setColorFilter(cf);
        }
    }

    @Override
    public int getOpacity() {
        return states[state].getOpacity();
    }
    
    @Override
    public void setBounds(Rect bounds) {
        for (Drawable state : states) {
            state.setBounds(bounds);
        }
    }
    
    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        for (Drawable state : states) {
            state.setBounds(left, top, right, bottom);
        }
    }

}
