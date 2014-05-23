package edu.rochester.nbook.monopdroid.board.surface;

import java.util.ArrayList;

import edu.rochester.nbook.monopdroid.board.Button;
import edu.rochester.nbook.monopdroid.board.Configurable;
import edu.rochester.nbook.monopdroid.board.Estate;
import edu.rochester.nbook.monopdroid.board.GameStatus;
import edu.rochester.nbook.monopdroid.board.Player;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;

public class BoardView extends SurfaceView {
    // ui thread exclusives
    private Thread surfaceThread = null;
    private ScaleGestureDetector scaleDetector = null;
    private GestureDetector scrollDetector = null;
    private BoardViewSurfaceThread surfaceRunner = null;
    private BoardViewListener listener = null;

    public BoardViewSurfaceThread saveState() {
        return surfaceRunner;
    }
    
    public void restoreState(BoardViewSurfaceThread surfaceRunner) {
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

    public BoardView(Context context) {
        super(context);
        setFocusable(true);
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
    }

    public BoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFocusable(true);
    }

    private void privateInit() {
        surfaceRunner.setHolder(this.getHolder());
        surfaceRunner.setListener(listener);
        // spaces = new ArrayList<BoardView.Space>(40);
        scrollDetector = new GestureDetector(this.getContext(),
                new SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        //if (surfaceRunner.isFixed()) {
                        int x = (int) e.getX();
                        int y = (int) e.getY();
                        return surfaceRunner.onSingleTapUp(x, y);
                        //} else {
                        //    return false;
                        //}
                    }
            
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        //if (!surfaceRunner.isFixed()) {
                        //    int x = (int) e.getX();
                        //    int y = (int) e.getY();
                        //    return surfaceRunner.onSingleTapUp(x, y);
                        //} else {
                        //    return false;
                        //}
                        return false;
                    }
        
                    @Override
                    public void onShowPress(MotionEvent e) {
                        int x = (int) e.getX();
                        int y = (int) e.getY();
                        surfaceRunner.onShowPress(x, y);
                    }
        
                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                        if (!surfaceRunner.isFixed()) {
                            surfaceRunner.translate(distanceX, distanceY);
                            return true;
                        } else {
                            return false;
                        }
                    }
        
                    @Override
                    public void onLongPress(MotionEvent e) {
                        int x = (int) e.getX();
                        int y = (int) e.getY();
                        surfaceRunner.onLongPress(x, y);
                    }
                    
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        if (!surfaceRunner.isFixed()) {
                            surfaceRunner.onDoubleTap(e.getX(), e.getY());
                            return true;
                        }
                        return false;
                    }
        
                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }
                });
        
        scaleDetector = new ScaleGestureDetector(this.getContext(),
                new SimpleOnScaleGestureListener() {

                    @Override
                    public boolean onScaleBegin(ScaleGestureDetector detector) {
                        return !surfaceRunner.isFixed();
                    }

                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        surfaceRunner.scale(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
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
        boolean result = this.scrollDetector.onTouchEvent(event) || this.scaleDetector.onTouchEvent(event);
        switch (event.getActionMasked()) {
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            int x = (int) event.getX();
            int y = (int) event.getY();
            surfaceRunner.onShowPressUp(x, y);
            break;
        }
        return result;
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
        }
    }
    
    public boolean isRunning() {
        return surfaceRunner.isRunning();
    }

    public void createTextRegion(String string, boolean isError) {
        if (this.getWidth() > 0 && this.getHeight() > 0) {
            surfaceRunner.beginRegions(BoardViewSurfaceThread.LAYER_BACKGROUND);
            surfaceRunner.createTextRegion(string, isError);
            surfaceRunner.commitRegions(BoardViewSurfaceThread.LAYER_BACKGROUND);
        }
    }

    public void drawBoardRegions(ArrayList<Estate> estates, SparseArray<Player> players) {
        if (surfaceRunner.getStatus() == GameStatus.RUN) {
            surfaceRunner.beginRegions(BoardViewSurfaceThread.LAYER_BACKGROUND);
            surfaceRunner.addEstateRegions(estates, players);
            surfaceRunner.commitRegions(BoardViewSurfaceThread.LAYER_BACKGROUND);
        }
    }
    
    public void drawActionRegions(ArrayList<Estate> estates, int[] playerIds, SparseArray<Player> players, ArrayList<Button> buttons, int selfPlayerId) {
        if (surfaceRunner.getStatus() == GameStatus.RUN) {
            surfaceRunner.beginRegions(BoardViewSurfaceThread.LAYER_TURN);
            surfaceRunner.addTurnRegions(estates, playerIds, players, buttons, selfPlayerId);
            surfaceRunner.commitRegions(BoardViewSurfaceThread.LAYER_TURN);
        }
    }
    
    public void drawPieces(ArrayList<Estate> estates, int[] playerIds, SparseArray<Player> players) {
        if (surfaceRunner.getStatus() == GameStatus.RUN) {
            surfaceRunner.beginRegions(BoardViewSurfaceThread.LAYER_PIECES);
            surfaceRunner.addPieceRegions(estates, playerIds, players);
            surfaceRunner.commitRegions(BoardViewSurfaceThread.LAYER_PIECES);
        }
    }

    public void drawConfigRegions(SparseArray<Configurable> configurables, boolean isMaster) {
        if (surfaceRunner.getStatus() == GameStatus.CONFIG) {
            surfaceRunner.beginRegions(BoardViewSurfaceThread.LAYER_BACKGROUND);
            surfaceRunner.addConfigurableRegions(configurables);
            surfaceRunner.addStartButtonRegions(isMaster);
            surfaceRunner.commitRegions(BoardViewSurfaceThread.LAYER_BACKGROUND);
        }
    }

    /*public void redrawConfigRegions(SparseArray<Configurable> configurables, boolean isMaster) {
        if (surfaceRunner.getStatus() == GameStatus.CONFIG) {
            surfaceRunner.updateConfigurableRegions(configurables);
            surfaceRunner.updateStartButtonRegions(isMaster);
        }
    }*/

    public void overlayPlayerInfo(int playerId) {
        surfaceRunner.beginRegions(BoardViewSurfaceThread.LAYER_OVERLAY);
        surfaceRunner.openOverlay(BoardViewOverlay.PLAYER, playerId);
        surfaceRunner.commitRegions(BoardViewSurfaceThread.LAYER_OVERLAY);
    }

    public void overlayEstateInfo(int estateId) {
        surfaceRunner.beginRegions(BoardViewSurfaceThread.LAYER_OVERLAY);
        surfaceRunner.openOverlay(BoardViewOverlay.ESTATE, estateId);
        surfaceRunner.commitRegions(BoardViewSurfaceThread.LAYER_OVERLAY);
    }

    public void overlayAuctionInfo(int auctionId) {
        surfaceRunner.beginRegions(BoardViewSurfaceThread.LAYER_OVERLAY);
        surfaceRunner.openOverlay(BoardViewOverlay.AUCTION, auctionId);
        surfaceRunner.commitRegions(BoardViewSurfaceThread.LAYER_OVERLAY);
    }

    public void overlayTradeInfo(int tradeId) {
        surfaceRunner.beginRegions(BoardViewSurfaceThread.LAYER_OVERLAY);
        surfaceRunner.openOverlay(BoardViewOverlay.TRADE, tradeId);
        surfaceRunner.commitRegions(BoardViewSurfaceThread.LAYER_OVERLAY);
    }

    public void redrawOverlay() {
        BoardViewOverlay overlay = surfaceRunner.getOverlay();
        int overlayObjectId = surfaceRunner.getOverlayObjectId();
        if (overlay == BoardViewOverlay.NONE) {
            surfaceRunner.closeOverlay();
        } else {
            surfaceRunner.beginRegions(BoardViewSurfaceThread.LAYER_OVERLAY);
            surfaceRunner.openOverlay(overlay, overlayObjectId);
            surfaceRunner.commitRegions(BoardViewSurfaceThread.LAYER_OVERLAY);
        }
    }

    public void closeOverlay() {
        surfaceRunner.closeOverlay();
    }

    public void calculateTextRegions() {
        surfaceRunner.calculateTextRegions();
    }

    public void calculateConfigRegions() {
        surfaceRunner.calculateConfigRegions();
    }

    public void calculateBoardRegions() {
        surfaceRunner.calculateBoardRegions();
    }

    public boolean isOverlayOpen() {
        return surfaceRunner.isOverlayOpen();
    }

    public void waitDraw() {
        surfaceRunner.waitDraw();
    }
}
