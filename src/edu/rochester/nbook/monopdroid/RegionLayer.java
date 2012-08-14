package edu.rochester.nbook.monopdroid;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;

public class RegionLayer {
    private List<Region> regions = new ArrayList<Region>();
    private List<Region> newRegions = null;
    private int index = 0;
    private boolean visible = true;
    private boolean fixed = true;
    
    public RegionLayer(int layerLevel, boolean fixed, boolean visible) {
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
        newRegions = new ArrayList<Region>();
    }
    
    public void addRegion(Region region) {
        newRegions.add(region);
    }
    
    public void commitRegions() {
        synchronized (regions) {
            regions = newRegions;
        }
    }
    
    public void drawRegions(Canvas canvas) {
        if (visible) {
            synchronized (regions) {
                for (Region region : regions) {
                    region.draw(canvas);
                }
            }
        }
    }

    public boolean isFixed() {
        return fixed;
    }
    
    public int getIndex() {
        return index;
    }

    public List<Region> getRegions() {
        return regions;
    }
}
