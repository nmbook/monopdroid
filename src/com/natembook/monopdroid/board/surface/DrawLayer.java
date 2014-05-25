package com.natembook.monopdroid.board.surface;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

public class DrawLayer {
    private ArrayList<Drawable> regions = new ArrayList<Drawable>();
    private ArrayList<Drawable> newRegions = null;
    private SparseArray<GestureRegion> gestureRegion = new SparseArray<GestureRegion>();
    private int index = 0;
    private boolean visible = true;
    private boolean fixed = true;
    
    public DrawLayer(int layerLevel, boolean fixed, boolean visible) {
        this.index = layerLevel;
        this.fixed = fixed;
        this.visible = visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void beginRegions() {
        newRegions = new ArrayList<Drawable>();
        gestureRegion = new SparseArray<GestureRegion>();
    }
    
    public void commitRegions() {
        synchronized (regions) {
            regions = newRegions;
        }
    }

    public void clearRegions() {
        synchronized (regions) {
            regions = new ArrayList<Drawable>();
        }
    }
    
    public void addDrawable(Drawable region) {
        newRegions.add(region);
    }
    
    public void addGestureRegion(GestureRegion region) {
        gestureRegion.put(region.getTag(), region);
    }
    
    public GestureRegion getGestureRegion(int tag) {
        return gestureRegion.get(tag);
    }

    public void clearGestureRegions() {
        gestureRegion.clear();
    }
    
    public void drawRegions(Canvas canvas) {
        if (visible) {
            //Paint p = new Paint();
            //p.setColor(Color.YELLOW);
            //p.setStyle(Paint.Style.STROKE);
            //p.setStrokeWidth(2);
            synchronized (regions) {
                for (Drawable d : regions) {
                    d.draw(canvas);
                }
                //for (Drawable d : regions) {
                //    canvas.drawRect(d.getBounds(), p);
                //}
            }
        }
    }

    public boolean isFixed() {
        return fixed;
    }
    
    public int getIndex() {
        return index;
    }

    public ArrayList<Drawable> getRegions() {
        return regions;
    }
    
    public SparseArray<GestureRegion> getGestureRegions() {
        return gestureRegion;
    }
}
