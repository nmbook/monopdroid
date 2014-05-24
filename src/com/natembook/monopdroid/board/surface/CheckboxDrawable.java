package com.natembook.monopdroid.board.surface;

import com.natembook.monopdroid.R;
import android.content.Context;
import android.graphics.drawable.Drawable;

public class CheckboxDrawable extends ButtonDrawable {
    public CheckboxDrawable(Context context) {
        super(context, 0);
        states = new Drawable[10];
        states[0] = context.getResources().getDrawable(R.drawable.btn_check_off_holo_dark);
        states[1] = context.getResources().getDrawable(R.drawable.btn_check_off_pressed_holo_dark);
        states[2] = context.getResources().getDrawable(R.drawable.btn_check_off_focused_holo_dark);
        states[3] = context.getResources().getDrawable(R.drawable.btn_check_off_disabled_holo_dark);
        states[4] = context.getResources().getDrawable(R.drawable.btn_check_off_disabled_focused_holo_dark);
        states[5] = context.getResources().getDrawable(R.drawable.btn_check_on_holo_dark);
        states[6] = context.getResources().getDrawable(R.drawable.btn_check_on_pressed_holo_dark);
        states[7] = context.getResources().getDrawable(R.drawable.btn_check_on_focused_holo_dark);
        states[8] = context.getResources().getDrawable(R.drawable.btn_check_on_disabled_holo_dark);
        states[9] = context.getResources().getDrawable(R.drawable.btn_check_on_disabled_focused_holo_dark);
        state = 0;
    }
}
