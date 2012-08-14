package edu.rochester.nbook.monopdroid;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;

public class BoardView extends SurfaceView {
    // ui thread exclusives
    private Thread surfaceThread = null;
    private ScaleGestureDetector scaleDetector = null;
    private GestureDetector scrollDetector = null;
    private BoardViewSurfaceThread surfaceRunner = null;
    private BoardViewListener listener = null;

    public BoardViewSurfaceThread getSurfaceRunner() {
        return surfaceRunner;
    }
    
    public void setSurfaceRunner(BoardViewSurfaceThread surfaceRunner) {
        this.surfaceRunner = surfaceRunner;
    }
    
    public BoardViewListener getBoardViewListener() {
        return surfaceRunner.getListener();
    }

    public void setBoardViewListener(BoardViewListener listener) {
        this.listener = listener;
        if (surfaceRunner != null) {
            surfaceRunner.setListener(listener);
        }
    }

    public GameStatus getStatus() {
        return surfaceRunner.getStatus();
    }

    public void setStatus(GameStatus status) {
        Log.d("monopd", "surface: state = " + status.toString());
        surfaceRunner.setStatus(status);
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void privateInit() {
        surfaceRunner.setHolder(this.getHolder());
        surfaceRunner.setListener(listener);
        // spaces = new ArrayList<BoardView.Space>(40);
        scrollDetector = new GestureDetector(new GestureDetector.OnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                int x = (int) e.getX() - (int) surfaceRunner.getOffsetX();
                int y = (int) e.getY() - (int) surfaceRunner.getOffsetY();
                surfaceRunner.onSingleTapUp(x, y);
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                int x = (int) e.getX() - (int) surfaceRunner.getOffsetX();
                int y = (int) e.getY() - (int) surfaceRunner.getOffsetY();
                surfaceRunner.onShowPress(x, y);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (!surfaceRunner.isFixed()) {
                    surfaceRunner.doTranslate(distanceX, distanceY);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onLongPress(MotionEvent e) {
                int x = (int) e.getX() - (int) surfaceRunner.getOffsetX();
                int y = (int) e.getY() - (int) surfaceRunner.getOffsetY();
                surfaceRunner.onLongPress(x, y);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });

        scaleDetector = new ScaleGestureDetector(this.getContext(),
                new ScaleGestureDetector.OnScaleGestureListener() {

                    @Override
                    public void onScaleEnd(ScaleGestureDetector detector) {
                    }

                    @Override
                    public boolean onScaleBegin(ScaleGestureDetector detector) {
                        return !surfaceRunner.isFixed();
                    }

                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        surfaceRunner.scale(detector.getScaleFactor());
                        return true;
                    }
                });
    }

    public void onResume() {
        if (surfaceRunner == null) {
            surfaceRunner = new BoardViewSurfaceThread(this.getContext(), this.getWidth(), this.getHeight());
        }
        privateInit();
        //this.surfaceRunner.setRunning(true);
        this.surfaceThread = new Thread(surfaceRunner);
        this.surfaceThread.start();
    }

    public void onPause() {
        boolean retry = true;
        this.surfaceRunner.setRunning(false);
        while (retry) {
            try {
                this.surfaceThread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.scrollDetector.onTouchEvent(event) || this.scaleDetector.onTouchEvent(event);
    }

    /**
     * Returns a square view with maximum size for this area.
     */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int measuredHeight = View.MeasureSpec.getSize(heightMeasureSpec);
        measuredWidth = measuredHeight = Math.min(measuredWidth, measuredHeight);
        this.setMeasuredDimension(measuredWidth, measuredHeight);
        if (surfaceRunner != null &&
                (this.getWidth() != 0 || this.getHeight() != 0)) {
            surfaceRunner.setSize(this.getWidth(), this.getHeight());
            listener.onResize(this.getWidth(), this.getHeight());
        }
    }
    
    public boolean isRunning() {
        return surfaceRunner.isRunning();
    }

    public void createTextRegion(String string) {
        surfaceRunner.beginRegions(BoardViewSurfaceThread.LAYER_BACKGROUND);
        surfaceRunner.createTextRegion(string);
        surfaceRunner.commitRegions(BoardViewSurfaceThread.LAYER_BACKGROUND);
    }

    public void drawBoardRegions(ArrayList<Estate> estates) {
        if (surfaceRunner.getStatus() == GameStatus.RUN) {
            surfaceRunner.beginRegions(BoardViewSurfaceThread.LAYER_BACKGROUND);
            surfaceRunner.addEstateRegions(estates);
            surfaceRunner.commitRegions(BoardViewSurfaceThread.LAYER_BACKGROUND);
        }
    }
    
    public void drawEstateOwnerRegions(ArrayList<Estate> estates, int[] playerIds, SparseArray<Player> players) {
        if (surfaceRunner.getStatus() == GameStatus.RUN) {
            surfaceRunner.beginRegions(BoardViewSurfaceThread.LAYER_OWNERS);
            surfaceRunner.addEstateHouseRegions(estates, players);
            surfaceRunner.addTurnRegions(playerIds, players);
            surfaceRunner.commitRegions(BoardViewSurfaceThread.LAYER_OWNERS);
        }
    }
    
    public void drawPieces(ArrayList<Estate> estates, int[] playerIds, SparseArray<Player> players) {
        if (surfaceRunner.getStatus() == GameStatus.RUN) {
            surfaceRunner.beginRegions(BoardViewSurfaceThread.LAYER_PIECES);
            surfaceRunner.addPieceRegions(estates, playerIds, players);
            surfaceRunner.commitRegions(BoardViewSurfaceThread.LAYER_PIECES);
        }
    }

    public void drawConfigRegions(List<Configurable> configurables, boolean isMaster) {
        if (surfaceRunner.getStatus() == GameStatus.CONFIG) {
            surfaceRunner.beginRegions(BoardViewSurfaceThread.LAYER_BACKGROUND);
            surfaceRunner.addConfigurableRegions(configurables);
            surfaceRunner.addStartButtonRegions(isMaster);
            surfaceRunner.commitRegions(BoardViewSurfaceThread.LAYER_BACKGROUND);
        }
    }

    public void overlayPlayerInfo(Player player) {
        surfaceRunner.beginRegions(BoardViewSurfaceThread.LAYER_OVERLAY);
        surfaceRunner.setEstateButtonsEnabled(false);
        surfaceRunner.addOverlayRegion();
        surfaceRunner.addPlayerOverlayRegions(player);
        surfaceRunner.commitRegions(BoardViewSurfaceThread.LAYER_OVERLAY);
    }

    public void overlayEstateInfo(Estate estate) {
        surfaceRunner.beginRegions(BoardViewSurfaceThread.LAYER_OVERLAY);
        surfaceRunner.setEstateButtonsEnabled(false);
        surfaceRunner.addOverlayRegion();
        surfaceRunner.addEstateOverlayRegions(estate);
        surfaceRunner.commitRegions(BoardViewSurfaceThread.LAYER_OVERLAY);
    }
}
