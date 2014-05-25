package com.natembook.monopdroid.board.surface;

public abstract class GestureRegionListener {
    /**
     * Called when this region is clicked. If not overridden, does nothing.
     * @param gestureRegion The region object.
     */
    public void onGestureRegionClick(GestureRegion region) {
        // default: do nothing
    }

    /**
     * Called when this region is long-pressed. If not overridden, does nothing.
     * @param region The region object.
     */
    public void onGestureRegionLongPress(GestureRegion region) {
        // default: do nothing
    }
}
