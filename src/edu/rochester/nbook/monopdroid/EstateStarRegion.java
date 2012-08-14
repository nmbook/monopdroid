package edu.rochester.nbook.monopdroid;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class EstateStarRegion extends Region {
    private Paint paint;
    
    /**
     * A star.
     */
    private static final Path path = new Path() {
        {
            moveTo(5, 0);
            lineTo(6, 4);
            lineTo(10, 4);
            lineTo(6, 6);
            lineTo(8, 10);
            lineTo(5, 7);
            lineTo(2, 10);
            lineTo(4, 6);
            lineTo(0, 4);
            lineTo(4, 4);
            lineTo(5, 0);
        }
    };

    public EstateStarRegion(RectF bounds, int tag, Paint paint) {
        super(bounds, tag);
        this.paint = paint;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPath(path, paint);
    }
}
