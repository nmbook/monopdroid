package com.natembook.monopdroid.board.surface;

import java.util.ArrayList;

import com.natembook.monopdroid.board.BoardActivity;
import com.natembook.monopdroid.board.Button;
import com.natembook.monopdroid.board.Configurable;
import com.natembook.monopdroid.board.Estate;
import com.natembook.monopdroid.board.GameStatus;
import com.natembook.monopdroid.board.OverlayButton;
import com.natembook.monopdroid.board.Player;
import com.natembook.monopdroid.board.surface.BoardViewPiece.For;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Handler;
import android.text.Layout.Alignment;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.SurfaceHolder;

public class BoardViewSurfaceThread implements Runnable {
    // tags for GestureRegions, all visible regions must have a unique tag
    //private static final int TAG_LOADING_TEXT = 0; // unique
    private static final int TAG_GAME_START_BUTTON = 1; // unique
    private static final int TAG_CONFIG_ITEM_CHECK = 2; // up to 16
    private static final int TAG_ESTATE = 18; // up to 40
    private static final int TAG_TURN_BUTTON_1 = 58; // unique
    private static final int TAG_TURN_BUTTON_2 = 59; // unique
    private static final int TAG_TURN_BUTTON_3 = 60; // unique
    private static final int TAG_OVERLAY = 61; // unique
    private static final int TAG_OVERLAY_BUTTON = 62; // up to 6
    
    // IDs for layers
    public static final int LAYER_BACKGROUND = 0;
    public static final int LAYER_TURN = 1;
    public static final int LAYER_PIECES = 2;
    public static final int LAYER_OVERLAY = 3;

    // indices in the rect/point calculation storage arrays
    private static final int DRAW_REGION_CENTER_BOUNDS = 0;
    private static final int DRAW_REGION_TEXT_BOUNDS = 1;
    private static final int DRAW_REGION_TEXT_COUNT = 2;
    private static final int DRAW_REGION_CONFIG_BUTTON_BOUNDS = 1;
    private static final int DRAW_REGION_CONFIG_CHECK_BOX_BOUNDS = 2;
    private static final int DRAW_REGION_CONFIG_CHECK_TEXT_BOUNDS = 18;
    private static final int DRAW_REGION_CONFIG_COUNT = 34;
    private static final int DRAW_REGION_BOARD_ESTATE_BOUNDS = 1;
    private static final int DRAW_REGION_BOARD_ESTATE_GRAD_BOUNDS = 41;
    private static final int DRAW_REGION_BOARD_COUNT = 81;
    
    private static final int DRAW_POINT_TEXT_COUNT = 0;
    private static final int DRAW_POINT_CONFIG_COUNT = 0;
    private static final int DRAW_POINT_BOARD_ESTATE_DIRECTION_OFFSET = 0;
    private static final int DRAW_POINT_BOARD_ESTATE_PIECE_POSITION = 40;
    private static final int DRAW_POINT_BOARD_ESTATE_ICON_POSITION = 80;
    private static final int DRAW_POINT_BOARD_ESTATE_HOUSE_POSITION = 120;
    private static final int DRAW_POINT_BOARD_ESTATE_PIECE_RADIUS = 160;
    private static final int DRAW_POINT_BOARD_ESTATE_ICON_RADIUS = 161;
    private static final int DRAW_POINT_BOARD_ESTATE_HOUSE_RADIUS = 162;
    private static final int DRAW_POINT_BOARD_COUNT = 163;
    
    private static final int DPI_SIZE_TEXT = 18;
    private static final int DPI_LINE_HEIGHT = 24;
    private static final int DPI_BUTTON_HEIGHT = 48;

    /**
     * Approximation of mathematical constant PHI.
     */
    private static final float phi = 1.618033988749894f;
    
    // current draw state
    private DrawState drawState = DrawState.NOTREADY;
    // calculated drawing rectangles for the current canvas size
    private Rect[] drawRegions;
    // calculated drawing points for the current canvas size
    private Point[] drawPoints;
    
    private volatile boolean hasChanges = false;
    private volatile boolean waitDraw = false;
    
    // has cached data been initialized yet?
    private static boolean staticInit = false;
    
    // draw thread cached images
    //private static SparseArray<GradientDrawable> estateGradientCache = new SparseArray<GradientDrawable>();
    
    /*private static Path housePath = new Path();
    {
        housePath.moveTo(0, 4);
        housePath.lineTo(2, 4);
        housePath.lineTo(2, 1);
        housePath.lineTo(1, 0);
        housePath.lineTo(0, 1);
        housePath.close();
    }*/
    
    private BoardViewOverlay overlay = BoardViewOverlay.NONE;
    private int overlayObjectId = -1;
    private GestureRegion currentEstateRegion = null;
    
    // draw thread paints
    public static Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //public static Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static Paint textNegativePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static Paint estateBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static Paint estateBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static Paint playerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    private ArrayList<Integer> configIndexMap = null; 
    
    // whether we are in the foreground
    private boolean running = false;
    // game status
    private GameStatus status = GameStatus.CREATE;
    // layers init
    private ArrayList<DrawLayer> layers = new ArrayList<DrawLayer>() {
        private static final long serialVersionUID = 7072660638043030076L;

        {
            add(new DrawLayer(LAYER_BACKGROUND, false, true));
            add(new DrawLayer(LAYER_TURN, false, true));
            add(new DrawLayer(LAYER_PIECES, false, true));
            add(new DrawLayer(LAYER_OVERLAY, true, false));
        }
    };
    // pinch-zoom state
    private PZState pzState = new PZState();
    // whether the PZ-state should be ignored at this time
    private boolean pzFixed = true;
    
    // width and height of the internal data
    private int width = 0;
    private int height = 0;
    
    private enum DrawState {
        NOTREADY, TEXT, CONFIG, BOARD
    }
    
    private Context context;
    
    public static final int animationSteps = 3;
    
    private SurfaceHolder surfaceHolder = null;
    private BoardViewListener listener = null;
    
    public void setListener(BoardViewListener listener) {
        this.listener = listener;
    }

    public BoardViewListener getListener() {
        return listener;
    }
    
    public void setHolder(SurfaceHolder holder) {
        surfaceHolder = holder;
    }
    
    public BoardViewSurfaceThread(Context context, int width, int height) {
        this.context = context;
        if (staticInit) {
            return;
        } else {
            staticInit = true;
        }
        bgPaint.setStyle(Style.FILL);
        bgPaint.setColor(Color.BLACK);
        /*
        textPaint.setStyle(Style.STROKE);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24);
        textNegativePaint.setColor(Color.BLACK);
        textNegativePaint.setTextSize(24);
        highlightPaint.setStyle(Style.STROKE);
        highlightPaint.setColor(Color.YELLOW);
        highlightPaint.setStrokeWidth(2f);
        estateBgPaint.setColor(Color.rgb(128, 192, 255));
        estateBgPaint.setStyle(Style.FILL);
        estateBorderPaint.setStyle(Style.STROKE);
        estateBorderPaint.setColor(Color.BLACK);
        estateBorderPaint.setStrokeWidth(3f);
        overlayPaint.setStyle(Style.FILL);
        playerPaint.setStyle(Style.FILL);*/
    }

    @Override
    public void run() {
        Log.d("monopd", "surface: Surface thread init");
        
        this.running = true;

        // Paint oPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // oPaint.setColor(Color.YELLOW);
        // oPaint.setStyle(Paint.Style.STROKE);
        while (this.running) {
            if (this.surfaceHolder.getSurface().isValid() && hasChanges) {
                Log.v("monopd", "surface: begin draw");
                Canvas canvas = this.surfaceHolder.lockCanvas();
                canvas.drawRect(new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), bgPaint);
                for (DrawLayer layer : this.layers) {
                    canvas.save();
                    if (!this.pzFixed && !layer.isFixed()) {
                        canvas.scale(pzState.scale, pzState.scale);
                        canvas.translate(pzState.offsetX, pzState.offsetY);
                    }
                    layer.drawRegions(canvas);
                    canvas.restore();
                }
                this.surfaceHolder.unlockCanvasAndPost(canvas);
                hasChanges = false;
                waitDraw = false;
                Log.v("monopd", "surface: end draw");
            }
            try {
                // 20 fps
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
        resetPZState();
        configIndexMap = null;
        this.pzFixed = status != GameStatus.RUN;
    }

    public boolean isFixed() {
        if (overlay != BoardViewOverlay.NONE) {
            return true;
        }
        return pzFixed;
    }
    
    private class PZState {
        private float offsetX;
        private float offsetY;
        private float scale;
        
        public PZState() {
            this.offsetX = this.offsetY = 0;
            this.scale = 1;
        }
        
        public PZState(float offsetX, float offsetY, float scale) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.scale = scale;
        }

        public float getOffsetX() {
            return offsetX;
        }

        public float getOffsetY() {
            return offsetY;
        }

        public float getScale() {
            return scale;
        }
        
        public void snapBounds() {
            if (offsetX > 0) {
                offsetX = 0;
            }
            if (offsetY > 0) {
                offsetY = 0;
            }
            float xWall = -width + width / scale;
            float yWall = -height + height / scale;
            if (offsetX < xWall) {
                offsetX = xWall;
            }
            if (offsetY < yWall) {
                offsetY = yWall;
            }
        }

        public PZState[] makeAnimationSteps(PZState previousState, int stepsToTake) {
            PZState[] steps = new PZState[stepsToTake];
            for (int i = 0; i < stepsToTake; i++) {
                float step = ((float)i + 1f) / (float)stepsToTake;
                steps[i] = new PZState(
                        (offsetX - previousState.offsetX) * step + previousState.offsetX,
                        (offsetY - previousState.offsetY) * step + previousState.offsetY,
                        (scale - previousState.scale) * step + previousState.scale
                        );
            }
            return steps;
        }
        
        @Override
        public String toString() {
            return "(" + offsetX + "," + offsetY + "):" + scale + "x";
        }
    }

    private int applyPZStateY(int y) {
        return (int)((float)y/pzState.scale - pzState.offsetY);
    }

    private int applyPZStateX(int x) {
        return (int)((float)x/pzState.scale - pzState.offsetX);
    }
    
    private void resetPZState() {
        pzState = new PZState();
        hasChanges = true;
    }
    
    private void commitPZState(PZState state) {
        pzAnimRunnable = null;
        pzState = state;
        hasChanges = true;
    }
    
    private int pzAnimIndex = 0;
    private Runnable pzAnimRunnable = null;
    
    private static final float pzMaxScale = 2f;
    private static final float pzMinScale = 1f;
    private static final int pzAnimSteps = 5;
    private static final int pzAnimMS = 5;
    
    private void animateToPZState(PZState state, final int stepsToTake, final int msToStep) {
        final PZState[] steps = state.makeAnimationSteps(pzState, stepsToTake);
        pzAnimIndex = 0;
        pzAnimRunnable = null;
        final Handler h = new Handler();
        pzAnimRunnable = new Runnable() {
            @Override
            public void run() {
                if (pzAnimIndex >= steps.length) {
                    return;
                }
                pzState = steps[pzAnimIndex];
                hasChanges = true;
                pzAnimIndex++;
                if (pzAnimIndex < stepsToTake) {
                    if (pzAnimRunnable != null) {
                        h.postDelayed(pzAnimRunnable, msToStep);
                    }
                }
            }
        };
        h.postDelayed(pzAnimRunnable, msToStep);
    }
    
    public void translate(float distanceX, float distanceY) {
        if (overlay != BoardViewOverlay.NONE) {
            return;
        }
        float offsetX = pzState.getOffsetX();
        float offsetY = pzState.getOffsetY();
        float scale = pzState.getScale();
        PZState state = new PZState(
                offsetX - distanceX / pzState.getScale(),
                offsetY - distanceY / pzState.getScale(),
                scale);
        state.snapBounds();
        commitPZState(state);
    }

    public void scale(float scaleFactor, float centerX, float centerY) {
        if (overlay != BoardViewOverlay.NONE) {
            return;
        }
        float offsetX = pzState.getOffsetX();
        float offsetY = pzState.getOffsetY();
        float scale = pzState.getScale();
        offsetX -= centerX / scale;
        offsetY -= centerY / scale;
        scale *= scaleFactor;
        if (scale < pzMinScale) {
            scale = pzMinScale;
        } else if (scale > pzMaxScale) {
            scale = pzMaxScale;
        }
        offsetX -= -centerX / scale;
        offsetY -= -centerY / scale;
        PZState state = new PZState(offsetX, offsetY, scale);
        state.snapBounds();
        commitPZState(state);
    }
    
    private void zoomOut() {
        PZState state = new PZState();
        animateToPZState(state, pzAnimSteps, pzAnimMS);
    }
    
    private void zoomIn(float centerX, float centerY) {
        float offsetX = pzState.getOffsetX();
        float offsetY = pzState.getOffsetY();
        float scale = pzState.getScale();
        offsetX -= centerX / scale;
        offsetY -= centerY / scale;
        scale = pzMaxScale;
        offsetX -= -centerX / scale;
        offsetY -= -centerY / scale;
        PZState state = new PZState(offsetX, offsetY, scale);
        state.snapBounds();
        animateToPZState(state, pzAnimSteps, pzAnimMS);
    }

    public void onDoubleTap(float x, float y) {
        if (overlay != BoardViewOverlay.NONE) {
            return;
        }
        if (pzState.scale == pzMinScale) {
            zoomIn(x, y);
        } else {
            zoomOut();
        }
    }

    public float getOffsetX() {
        return pzState.offsetX;
    }

    public float getOffsetY() {
        return pzState.offsetY;
    }
    
    /**
     * The interface for findRegionIntersect(x, y) to call when it finds a GestureRegion.
     * @author Nate
     *
     */
    private interface GestureRegionAction {
        /**
         * Called when the x and y of the touch event corresponds with a GestureRegion.
         * @param region The GestureRegion found.
         * @return Return true if the event should be consumed.
         */
        public boolean onGestureRegionIntersect(GestureRegion region);
    }
    
    /**
     * Call this to handle a touch event at a specified x and y (in surface coordinates).
     * This will apply the current PZ state and find a single GestureRegion to handle the event for,
     * iterating in order through the DrawLayers and backwards through the GestureRegions in each layer
     * (so that regions added in front of other regions are prioritized if at the same coordinates).
     * If an overlay is visible, only that layer will be checked.
     * When a GestureRegion is found, the given action will be executed.
     * @param x The x coordinate of the event.
     * @param y The y coordinate of the event.
     * @param action The onGestureRegionIntersect(region) event to call.
     * @return Returns true if a region was found and the onGestureRegionIntersect call returned true, false otherwise.
     */
    private boolean findRegionIntersect(int x, int y, GestureRegionAction action) {
        for (DrawLayer layer : layers) {
            if (overlay == BoardViewOverlay.NONE || layer.getIndex() == LAYER_OVERLAY) {
                if (!pzFixed && !layer.isFixed()) {
                    x = applyPZStateX(x);
                    y = applyPZStateY(y);
                }
                for (int i = layer.getGestureRegions().size() - 1; i >= 0; i--) {
                    GestureRegion region = layer.getGestureRegion(layer.getGestureRegions().keyAt(i));
                    if (region.getBounds().contains(x, y)) {
                        if (region.isEnabled()) {
                            return action.onGestureRegionIntersect(region);
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check intersections with enabled layers.
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return Whether the single tap succeeded.
     */
    public boolean onSingleTapUp(int x, int y) {
        return findRegionIntersect(x, y, new GestureRegionAction() {
            @Override
            public boolean onGestureRegionIntersect(GestureRegion region) {
                region.invokeClick();
                return true;
            }
        });
    }

    public void onLongPress(int x, int y) {
        findRegionIntersect(x, y, new GestureRegionAction() {
            @Override
            public boolean onGestureRegionIntersect(GestureRegion region) {
                if (region.isLongClickable()) {
                    region.invokeLongPress();
                    region.up();
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    public void onShowPress(int x, int y) {
        findRegionIntersect(x, y, new GestureRegionAction() {
            @Override
            public boolean onGestureRegionIntersect(GestureRegion region) {
                region.down();
                return true;
            }
        });
        hasChanges = true;
    }

    public void onShowPressUp(int x, int y) {
        findRegionIntersect(x, y, new GestureRegionAction() {
            @Override
            public boolean onGestureRegionIntersect(GestureRegion region) {
                region.up();
                return true;
            }
        });
        hasChanges = true;
    }

    public void commitRegions(int layer) {
        layers.get(layer).commitRegions();
        hasChanges = true;
    }

    public void beginRegions(int layer) {
        layers.get(layer).beginRegions();
    }

    public void clearRegions(int layer) {
        layers.get(layer).clearRegions();
        hasChanges = true;
    }

    public void setSize(int width, int height) {
        if (this.width != width && this.height != height) {
            this.width = width;
            this.height = height;
            
            listener.onResize(this.getWidth(), this.getHeight());
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ArrayList<DrawLayer> getRegions() {
        return this.layers;
    }
    
    public void calculateTextRegions() {
        if (width == 0 || height == 0) {
            return;
        }
        drawState = DrawState.TEXT;
        drawRegions = new Rect[DRAW_REGION_TEXT_COUNT];
        drawPoints = new Point[DRAW_POINT_TEXT_COUNT];
        
        drawRegions[DRAW_REGION_TEXT_BOUNDS] = new Rect(0, 0, width, height);
        //float part = ((float)width / ((4f * phi) + 18f));
        drawRegions[DRAW_REGION_CENTER_BOUNDS] =
                new Rect(width / 16 + 6, height / 16 + 6, width * 15 / 16 - 6, height * 15 / 16 - 6);
                //new Rect((int)(2f * phi * part), (int)(2f * phi * part), (int)(width - 2f * phi * part), (int)(height - 2f * phi * part));
    }
    
    public void calculateConfigRegions() {
        if (width == 0 || height == 0) {
            return;
        }
        drawState = DrawState.CONFIG;
        drawRegions = new Rect[DRAW_REGION_CONFIG_COUNT];
        drawPoints = new Point[DRAW_POINT_CONFIG_COUNT];

        int lineHeight = (int) getPixelSize(DPI_LINE_HEIGHT);
        int btnHeight = (int) getPixelSize(DPI_BUTTON_HEIGHT);
        
        drawRegions[DRAW_REGION_CONFIG_BUTTON_BOUNDS] =
                new Rect(60, height - 5 - btnHeight, width - 60, height - 5);
                //new Rect(60, height - 38, width - 60, height - 5);
        for (int index = 0; index < 16; index++) {
            drawRegions[DRAW_REGION_CONFIG_CHECK_BOX_BOUNDS + index] =
                    new Rect(5, 10 + (index * lineHeight), 40, lineHeight + (index * lineHeight));
            drawRegions[DRAW_REGION_CONFIG_CHECK_TEXT_BOUNDS + index] =
                    new Rect(45, 10 + (index * lineHeight), width, lineHeight + (index * lineHeight));
                    //new Rect(45, 32 + (index * 55), width, 55 + (index * 55));
        }
        //float part = ((float)width / ((4f * phi) + 18f));
        drawRegions[DRAW_REGION_CENTER_BOUNDS] =
                new Rect(width / 16 + 6, height / 16 + 6, width * 15 / 16 - 6, height * 15 / 16 - 6);
                //new Rect((int)(2f * phi * part), (int)(2f * phi * part), (int)(width - 2f * phi * part), (int)(height - 2f * phi * part));
    }

    private static boolean range(int value, int min, int max) {
        return (value >= min && value <= max);
    }
    
    public void calculateBoardRegions() {
        if (width == 0 || height == 0) {
            return;
        }
        drawState = DrawState.BOARD;
        drawRegions = new Rect[DRAW_REGION_BOARD_COUNT];
        drawPoints = new Point[DRAW_POINT_BOARD_COUNT];
        
        float part = ((float)width / ((4f * phi) + 18f));
        float radius = 0;
        for (int index = 0; index < 40; index++) {
            // calculate the regions...
            float estateX = 0, estateY = 0, estateW = 0, estateH = 0;
            float gradX = 0, gradY = 0, gradW = 0, gradH = 0;
            float iconX = 0, iconY = 0, pieceX = 0, pieceY = 0;
            float pieceDeltaX = 0, pieceDeltaY = 0;
            if (range(index, 0, 10)) {
                gradW = estateW = 2;
                gradH = estateH = (2f * phi);
                gradX = estateX = ((4f * phi) + 18f) - 2f * phi - (float)index * 2f;
                gradY = estateY = ((4f * phi) + 18f) - (2f * phi);
                gradH = 1;
                pieceX = iconX = estateX + 1f;
                pieceY = estateY + 2f;
                iconY = ((4f * phi) + 18f) - 1.4f;
                pieceDeltaX = 0.5f;
                pieceDeltaY = 0f;
            }
            if (range(index, 10, 20)) {
                gradW = estateW = 2f * phi;
                if (estateH == 0) {
                    gradH = estateH = 2;
                }
                gradX = estateX = 0;
                gradY = estateY = ((4f * phi) + 18f) - 2f * phi - ((float)index - 10f) * 2f;
                gradX = 2f * phi - 1f;
                gradW = 1;
                pieceX = estateX + 1f;
                pieceY = iconY = estateY + 1f;
                iconX = 1.4f;
                pieceDeltaX = 0f;
                pieceDeltaY = 0.5f;
            }
            if (range(index, 20, 30)) {
                if (estateW == 0) {
                    gradW = estateW = 2;
                    gradX = estateX = 2f * phi + ((float)index - 21f) * 2f;
                }
                gradH = estateH = 2f * phi;
                gradY = estateY = 0;
                gradY = 2f * phi - 1f;
                gradH = 1;
                pieceX = iconX = estateX + 1f;
                pieceY = estateY + 1f;
                iconY = 1.4f;
                pieceDeltaX = -0.5f;
                pieceDeltaY = 0f;
            }
            if (range(index, 30, 39) || index == 0) {
                gradW = estateW = 2f * phi;
                if (estateH == 0) {
                    gradH = estateH = 2;
                    gradY = estateY = 2f * phi + ((float)index - 31f) * 2f;
                }
                gradX = estateX = ((4f * phi) + 18f) - (2f * phi);
                gradW = 1;
                if (index != 0) {
                    pieceX = estateX + 2f;
                    pieceY = iconY = estateY + 1f;
                    iconX = ((4f * phi) + 18f) - 1.4f;
                    pieceDeltaX = 0f;
                    pieceDeltaY = -0.5f;
                }
            }
            estateX *= part;
            estateY *= part;
            estateW *= part;
            estateH *= part;
            gradX *= part;
            gradY *= part;
            gradW *= part;
            gradH *= part;
            iconX *= part;
            iconY *= part;
            pieceX *= part;
            pieceY *= part;
            pieceDeltaX *= part;
            pieceDeltaY *= part;
            radius = part * 0.6f;
            drawRegions[DRAW_REGION_BOARD_ESTATE_BOUNDS + index] =
                    new Rect((int)estateX, (int)estateY, (int)(estateX + estateW), (int)(estateY + estateH));
            drawRegions[DRAW_REGION_BOARD_ESTATE_GRAD_BOUNDS + index] =
                    new Rect((int)gradX, (int)gradY, (int)(gradX + gradW), (int)(gradY + gradH));
            drawPoints[DRAW_POINT_BOARD_ESTATE_DIRECTION_OFFSET + index] =
                    new Point((int)pieceDeltaX, (int)pieceDeltaY);
            drawPoints[DRAW_POINT_BOARD_ESTATE_PIECE_POSITION + index] =
                    new Point((int)pieceX, (int)pieceY);
            drawPoints[DRAW_POINT_BOARD_ESTATE_HOUSE_POSITION + index] =
                    new Point((int)pieceX, (int)pieceY);
            drawPoints[DRAW_POINT_BOARD_ESTATE_ICON_POSITION + index] =
                    new Point((int)iconX, (int)iconY);
        }
        drawPoints[DRAW_POINT_BOARD_ESTATE_HOUSE_RADIUS] =
                new Point((int)radius, (int)radius);
        drawPoints[DRAW_POINT_BOARD_ESTATE_ICON_RADIUS] =
                new Point((int)radius, (int)radius);
        drawPoints[DRAW_POINT_BOARD_ESTATE_PIECE_RADIUS] =
                new Point((int)radius, (int)radius);
        drawRegions[DRAW_REGION_CENTER_BOUNDS] =
                new Rect(width / 16 + 6, height / 16 + 6, width * 15 / 16 - 6, height * 15 / 16 - 6);
                //new Rect((int)(2f * phi * part), (int)(2f * phi * part), (int)(width - 2f * phi * part), (int)(height - 2f * phi * part));
    }

    public void createTextRegion(String string, boolean isError) {
        if (drawState == DrawState.NOTREADY) {
            return;
        }
        TextDrawable text = new TextDrawable(
                string,
                getPixelSize(DPI_SIZE_TEXT),
                Color.WHITE,
                Color.RED,
                Alignment.ALIGN_NORMAL,
                VerticalAlignment.VALIGN_TOP);
        text.setBounds(drawRegions[DRAW_REGION_TEXT_BOUNDS]);
        if (isError) {
            text.onStateChanged(ButtonState.DISABLED);
        } else {
            text.onStateChanged(ButtonState.NORMAL);
        }
        layers.get(LAYER_BACKGROUND).addDrawable(text);
    }

    public void addConfigurableRegions(final SparseArray<Configurable> configurables) {
        if (drawState == DrawState.NOTREADY) {
            return;
        }
        int index = 0;
        configIndexMap = new ArrayList<Integer>();
        for (int i = 0; i < configurables.size(); i++) {
            final int configId = configurables.keyAt(i);
            Configurable config = configurables.valueAt(i);
            index = configIndexMap.size();
            configIndexMap.add(configId);
            Rect encl = new Rect(drawRegions[DRAW_REGION_CONFIG_CHECK_BOX_BOUNDS + index]);
            encl.union(drawRegions[DRAW_REGION_CONFIG_CHECK_TEXT_BOUNDS + index]);
            CheckboxDrawable checkbox = new CheckboxDrawable(context);
            checkbox.setBounds(drawRegions[DRAW_REGION_CONFIG_CHECK_BOX_BOUNDS + index]);
            TextDrawable text = new TextDrawable(
                    config.getDescription(),
                    getPixelSize(DPI_SIZE_TEXT),
                    Color.WHITE,
                    Color.GRAY,
                    Alignment.ALIGN_NORMAL,
                    VerticalAlignment.VALIGN_MIDDLE);
            text.setBounds(drawRegions[DRAW_REGION_CONFIG_CHECK_TEXT_BOUNDS + index]);
            GestureRegion item = new GestureRegion(
                    encl,
                    TAG_CONFIG_ITEM_CHECK + index,
                    new GestureRegionListener() {
                        @Override
                        public void onGestureRegionClick(GestureRegion region) {
                            if (listener != null) {
                                for (int i = 0; i < configurables.size(); i++) {
                                    final int possibleConfigId = configurables.keyAt(i);
                                    Configurable possibleConfig = configurables.valueAt(i);
                                    if (possibleConfigId == configId) {
                                        listener.onConfigChange(possibleConfig.getCommand(),
                                                possibleConfig.getValue().equals("0") ? "1" : "0");
                                    }
                                }
                            }
                        }
                    });
            item.addStateHandler(checkbox);
            item.addStateHandler(text);
            if (config.isEditable()) {
                item.enable();
            } else {
                item.disable();
            }
            if (config.getValue().equals("0")) {
                item.uncheck();
            } else {
                item.check();
            }
            layers.get(LAYER_BACKGROUND).addDrawable(checkbox);
            layers.get(LAYER_BACKGROUND).addDrawable(text);
            layers.get(LAYER_BACKGROUND).addGestureRegion(item);
            index++;
        }
    }

    /*public void updateConfigurableRegions(SparseArray<Configurable> configurables) {
        for (int i = 0; i < configurables.size(); i++) {
            final int configId = configurables.keyAt(i);
            Configurable config = configurables.valueAt(i);
            int configIndex = configIndexMap.indexOf(configId);
            //if (configIndex >= 0) {
            GestureRegion region = layers.get(LAYER_BACKGROUND).getGestureRegion(TAG_CONFIG_ITEM_CHECK + configIndex);
            if (config.isEditable()) {
                region.enable();
            } else {
                region.disable();
            }
            if (config.getValue().equals("0")) {
                region.uncheck();
            } else {
                region.check();
            }
            //}
        }
        hasChanges = true;
    }*/

    public void addStartButtonRegions(boolean isMaster) {
        if (drawState == DrawState.NOTREADY) {
            return;
        }
        addButton(LAYER_BACKGROUND,
                drawRegions[DRAW_REGION_CONFIG_BUTTON_BOUNDS],
                "Start Game",
                isMaster,
                TAG_GAME_START_BUTTON, 
                new GestureRegionListener() {
                    @Override
                    public void onGestureRegionClick(GestureRegion region) {
                        listener.onStartGame();
                    }
            });
    }

    /*public void updateStartButtonRegions(boolean isMaster) {
        GestureRegion region = (GestureRegion) layers.get(LAYER_BACKGROUND).getGestureRegion(TAG_GAME_START_BUTTON);
        if (isMaster) {
            region.enable();
        } else {
            region.disable();
        }
        hasChanges = true;
    }&/
    
    /**
     * Specifies the side or corner of the board of this estate.
     * Has methods to 
     * @author Nate
     *
     */
    private enum EstateDirection {
        BOTTOM_RIGHT(315), BOTTOM(0),
        BOTTOM_LEFT(45), LEFT(90),
        TOP_LEFT(135), TOP(180),
        TOP_RIGHT(225), RIGHT(270);
        
        private int degrees;
        
        private EstateDirection(int degrees) {
            this.degrees = degrees;
        }
        
        /**
         * Gets the degrees to rotate a Drawable on an estate on this side of the board.
         * @return The degrees.  
         */
        public int getRotateDegrees() {
            return degrees;
        }
        
        /**
         * Calculates and returns the Orientation that a gradient should be if LEFT_RIGHT on TOP.
         * @return The calculated Orientation value.
         */
        public Orientation getGradientOrientation() {
            int oDegrees = degrees / 45 - 6;
            if (oDegrees < 0) {
                oDegrees += 8;
            }
            return Orientation.values()[oDegrees];
        }

        public static EstateDirection fromIndex(int index) {
            if (index == 0) {
                return BOTTOM_RIGHT;
            } else if (index == 10) {
                return BOTTOM_LEFT;
            } else if (index == 20) {
                return TOP_LEFT;
            } else if (index == 30) {
                return TOP_RIGHT;
            } else if (range(index, 1, 9)) {
                return BOTTOM;
            } else if (range(index, 11, 19)) {
                return LEFT;
            } else if (range(index, 21, 29)) {
                return TOP;
            } else if (range(index, 31, 39)) {
                return RIGHT;
            } else {
                return BOTTOM;
            }
        }
    };

    public void addEstateRegions(ArrayList<Estate> estates, SparseArray<Player> players) {
        if (drawState == DrawState.NOTREADY) {
            return;
        }
        for (int index = 0; index < 40; index++) {
            EstateDirection direction = EstateDirection.fromIndex(index);
            
            final int estateId = index;
            Estate estate = estates.get(index);
            Rect bounds = new Rect(drawRegions[DRAW_REGION_BOARD_ESTATE_BOUNDS + index]);
            GestureRegion region = new GestureRegion(
                    drawRegions[DRAW_REGION_BOARD_ESTATE_BOUNDS + index],
                    TAG_ESTATE + index,
                    new GestureRegionListener() {
                        @Override
                        public void onGestureRegionClick(GestureRegion gestureRegion) {
                            listener.onEstateClick(estateId);
                        }
                    });
            RectDrawable estateDraw = new RectDrawable(estate.getBgColor(), darken(estate.getBgColor()), Color.BLACK, 2);
            estateDraw.setBounds(new Rect(bounds));
            region.addStateHandler(estateDraw);
            region.enable();
            layers.get(LAYER_BACKGROUND).addGestureRegion(region);
            layers.get(LAYER_BACKGROUND).addDrawable(estateDraw);
            
            if (estate.getOwner() > 0) {
                int ownerColor = 0;
                int borderWidth = 8;
                int insetWidth = 5;
                BoardViewPiece piece = BoardViewPiece.getPiece(estate.getOwner());
                if (piece != null) {
                    ownerColor = piece.getColor();
                    if (estate.isMortgaged()) {
                        ownerColor = fade(ownerColor);
                        borderWidth = 4;
                        insetWidth = 3;
                    }
                }
                RectDrawable estateOwnerBorder = new RectDrawable(0, ownerColor, borderWidth);
                bounds.inset(insetWidth, insetWidth);
                estateOwnerBorder.setBounds(bounds);
                layers.get(LAYER_BACKGROUND).addDrawable(estateOwnerBorder);
            }
            
            if (estate.getColor() != 0) {
                Orientation gradOrient = direction.getGradientOrientation();
                GradientDrawable grad = createEstateGradient(estate.getColor(), gradOrient);
                RectDrawable estateGradientDraw = new RectDrawable(grad, Color.BLACK, 2);
                estateGradientDraw.setBounds(drawRegions[DRAW_REGION_BOARD_ESTATE_GRAD_BOUNDS + index]);
                layers.get(LAYER_BACKGROUND).addDrawable(estateGradientDraw);
                /*int color = Color.GREEN;
                int houseCount = estate.getHouses();
                if (houseCount == 5) {
                    houseCount = 1;
                    color = Color.RED;
                }
                Point offset = drawPoints[DRAW_POINT_BOARD_ESTATE_DIRECTION_OFFSET + index];
                float radius = drawPoints[DRAW_POINT_BOARD_ESTATE_HOUSE_RADIUS].x;
                for (int i = 0; i < houseCount; i++) {
                    Point location = new Point(drawPoints[DRAW_POINT_BOARD_ESTATE_PIECE_POSITION + index]);
                    float offsetScale = i - houseCount / 2f;
                    location.offset((int)(offset.x * offsetScale), (int)(offset.y * offsetScale));
                    ShapeDrawable house = new ShapeDrawable(new PathShape(housePath, radius, radius));
                    Rect houseBounds = new Rect(location.x - (int)radius, location.y - (int)radius, location.x + (int)radius, location.y + (int)radius);
                    house.getPaint().setShader(new RadialGradient(location.x, location.y, radius, color, darken(color), TileMode.CLAMP));
                    house.setBounds(houseBounds);
                    layers.get(LAYER_BACKGROUND).addDrawable(house);
                }*/
            }
            
            if (estate.getIcon() == null) {
                // force defaults on null icons!
                // the protocol appears unfinished for icons
                // so I set defaults if the icon attribute is not set on an estate.
                // it is backwards compatible with the two icons I've seen "dollar.png" and "qmark-red.png".
                // otherwise the icon is chosen based on estate ID assuming "Go" is estate ID 0.
                
                switch (estate.getEstateId()) {
                case 2: // COMMUNITY CHEST 1
                case 17: // COMMUNITY CHEST 2
                case 33: // COMMUNITY CHEST 3
                    estate.setIcon("c_chest.png");
                    break;
                case 4: // INCOME TAX
                case 38: // LUXURY TAX
                    estate.setIcon("tax.png");
                    break;
                case 5: // RAILROAD 1
                case 15: // RAILROAD 2
                case 25: // RAILROAD 3
                case 35: // RAILROAD 4
                    estate.setIcon("railroad.png");
                    break;
                case 7: // CHANCE 1
                case 22: // CHANCE 2
                case 36: // CHANCE 3
                    estate.setIcon("chance.png");
                    break;
                case 12: // ELECTRIC COMPANY
                    estate.setIcon("electric_co.png");
                    break;
                case 28: // WATER WORKS
                    estate.setIcon("water_wks.png");
                    break;
                default: // BLANK
                    estate.setIcon("");
                    break;
                }
            }
            
            int iconResource = 0;
            if (estate.getIcon().equals("")) { // blank
                iconResource = 0;
            } else if (estate.getIcon().equals("qmark-red.png") || estate.getIcon().equals("chance.png")) { // chance
                iconResource = 0; // R.drawable.qmark_red
            } else if (estate.getIcon().equals("dollar.png") || estate.getIcon().equals("tax.png")) { // tax
                iconResource = 0; // R.drawable.tax;
            } else if (estate.getIcon().equals("c_chest.png")) { // community chest
                iconResource = 0; // R.drawable.c_chest
            } else if (estate.getIcon().equals("railroad.png")) { // railroad
                iconResource = 0; // R.drawable.railroad;
            } else if (estate.getIcon().equals("electric_co.png")) { // elco
                iconResource = 0; //R.drawable.electric_co;
            } else if (estate.getIcon().equals("water_wks.png")) { // wawks
                iconResource = 0; //R.drawable.water_wks;
            }
            if (iconResource != 0) {
                Point iconCenter = drawPoints[DRAW_POINT_BOARD_ESTATE_ICON_POSITION + index];
                Drawable iconBitmap = context.getResources().getDrawable(iconResource).mutate();
                RotateDrawable iconBitmapRotator = new RotateDrawable(iconBitmap, direction.getRotateDegrees());
                float radius = drawPoints[DRAW_POINT_BOARD_ESTATE_ICON_RADIUS].x;
                Rect iconBounds = new Rect((int)iconCenter.x - (int)radius, (int)iconCenter.y - (int)radius, (int)iconCenter.x + (int)radius, (int)iconCenter.y + (int)radius);
                iconBitmapRotator.setBounds(iconBounds);
                layers.get(LAYER_BACKGROUND).addDrawable(iconBitmapRotator);
            }
        }
    }

    public void addEstateHouseRegions(ArrayList<Estate> estates, SparseArray<Player> players) {
        if (drawState == DrawState.NOTREADY) {
            return;
        }
        /*for (int i = 0; i < 40; i++) {
            Estate estate = estates.get(i);
            if (estate.canBeOwned()) {
                int owner = estate.getOwner();
                if (owner > 0) {
                    int playerIndex = 0;
                    for (int j = 0; j < BoardViewPiece.MAX_PLAYERS; j++) {
                        if (owner == BoardViewPiece.pieces[j].getPlayerId()) {
                            playerIndex = j;
                        }
                    }
                    Point piece = drawPoints[DRAW_POINT_BOARD_ESTATE_PIECE_POSITION + i];
                    float radius = drawPoints[DRAW_POINT_BOARD_ESTATE_PIECE_RADIUS].x;
                    Rect rect = new Rect(piece.x - (int)radius, piece.y - (int)radius, piece.x + (int)radius, piece.y + (int)radius);
                    int color = BoardViewPiece.pieces[playerIndex].getColor();
                    color = darken(color);
                    if (estate.isMortgaged()) {
                        color = darken(color);
                    }
                    GradientDrawable draw = BoardViewPiece.pieces[playerIndex].getDrawable();
                    draw.setBounds(rect);
                    layers.get(LAYER_OWNERS).addRegion(draw);
                }
            }
        }*/
    }

    public void addPieceRegions(ArrayList<Estate> estates, int[] playerIds, SparseArray<Player> players) {
        if (drawState == DrawState.NOTREADY) {
            return;
        }
        for (int i = 0; i < BoardViewPiece.MAX_PLAYERS; i++) {
            int playerId = BoardViewPiece.pieces[i].getPlayerId();
            int currentEstate = BoardViewPiece.pieces[i].getCurrentEstate();
            int progressEstate = BoardViewPiece.pieces[i].getProgressEstate();
            int progressEstateDelta = BoardViewPiece.pieces[i].getProgressEstateDelta();
            //Log.v("monopd", "draw: piece for player " + playerId +
            //        ", current estate " + currentEstate +
            //        ", progress estate " + progressEstate +
            //        ", progress delta estate " + progressEstateDelta);
            // location of piece = PROGRESSDELTA steps / animation steps, between PROGRESS and CURRENT
            if (playerId >= 0) {
                int sameEstateIndex = 0;
                int sameEstate = 0;
                for (int j = 0; j < BoardViewPiece.MAX_PLAYERS; j++) {
                    if (playerId > 0 &&
                            BoardViewPiece.pieces[j].getProgressEstate() == progressEstate) {
                        if (j < i) {
                            sameEstate++;
                            sameEstateIndex++;
                        } else if (j > i) {
                            sameEstate++;
                        }
                    }
                }
                // store values: amount and direction to move when overlapping other pieces
                Point offsetMultiplier = drawPoints[DRAW_POINT_BOARD_ESTATE_DIRECTION_OFFSET + progressEstate];
                // store values: piece radius
                float radius = drawPoints[DRAW_POINT_BOARD_ESTATE_PIECE_RADIUS].x;
                // store values: scale of overlapping-piece offset (based on how many pieces are overlapping us)
                float offsetScale = sameEstateIndex - (sameEstate) / 2f;
                // store values: point of Estate we are coming from (this is the last place we were)
                Point estatePointProgress = new Point(drawPoints[DRAW_POINT_BOARD_ESTATE_PIECE_POSITION + progressEstate]);
                // store values: point of Estate we are going to (we are currently here)
                Point estatePointCurrent = new Point(drawPoints[DRAW_POINT_BOARD_ESTATE_PIECE_POSITION + currentEstate]);
                /*if (estatePointProgress.equals(estatePointCurrent)) {
                    // if current == progress, then we are stationary on this estate
                    estatePointProgress.offset((int)(offsetMultiplier.x * offsetScale), (int)(offsetMultiplier.y * offsetScale));
                } else {
                    float progress = (float)progressEstateDelta / (float)animationSteps; 
                    estatePointCurrent.offset(-estatePointProgress.x, -estatePointProgress.y);
                    estatePointCurrent = new Point((int)(estatePointCurrent.x * progress), (int)(estatePointCurrent.y * progress));
                    estatePointCurrent.offset(estatePointProgress.x, estatePointProgress.y);
                    estatePointProgress = new Point(estatePointCurrent.x, estatePointCurrent.y);
                }*/
                Rect rect = new Rect((int)estatePointCurrent.x - (int)radius, (int)estatePointCurrent.y - (int)radius, (int)estatePointCurrent.x + (int)radius, (int)estatePointCurrent.y + (int)radius);
                Drawable draw = BoardViewPiece.pieces[i].getDrawable(For.BOARD);
                draw.setBounds(rect);
                layers.get(LAYER_PIECES).addDrawable(draw);
            }
        }
    }

    public boolean isOverlayOpen() {
        return overlay != BoardViewOverlay.NONE;
    }

    public void closeOverlay() {
        overlay = BoardViewOverlay.NONE;
        overlayObjectId = -1;
        layers.get(LAYER_OVERLAY).clearRegions();
        layers.get(LAYER_OVERLAY).clearGestureRegions();
        layers.get(LAYER_OVERLAY).setVisible(false);
        if (currentEstateRegion != null) {
            currentEstateRegion.unfocus();
            currentEstateRegion = null;
        }
        hasChanges = true;
    }

    public void addOverlayRegion() {
        if (drawState == DrawState.NOTREADY) {
            return;
        }

        if (currentEstateRegion != null) {
            currentEstateRegion.unfocus();
            currentEstateRegion = null;
        }
        
        layers.get(LAYER_OVERLAY).setVisible(true);
        
        GradientDrawable grOverlay = new GradientDrawable(Orientation.LEFT_RIGHT, new int[] { Color.argb(224, 0, 0, 0), Color.argb(192, 0, 0, 0) });
        grOverlay.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        //grOverlay.setGradientCenter(width / 2, height / 2);
        grOverlay.setShape(GradientDrawable.RECTANGLE);
        grOverlay.setSize(width, height);
        grOverlay.setGradientRadius(width / 2f);
        grOverlay.setBounds(0, 0, width, height);
        TextDrawable closeText = new TextDrawable(
                "<i>(tap anywhere to close overlay)</i>",
                getPixelSize(DPI_SIZE_TEXT),
                Color.WHITE, Color.WHITE,
                Alignment.ALIGN_OPPOSITE,
                VerticalAlignment.VALIGN_TOP);
        Rect ctBounds = new Rect(drawRegions[DRAW_REGION_CENTER_BOUNDS]);
        ctBounds = new Rect(ctBounds.left, ctBounds.bottom, ctBounds.right, height);
        closeText.setBounds(ctBounds);
        GestureRegion region = new GestureRegion(
                new Rect(0, 0, width, height),
                TAG_OVERLAY,
                new GestureRegionListener() {
                    @Override
                    public void onGestureRegionClick(GestureRegion gestureRegion) {
                        listener.onCloseOverlay();
                    }
                });
       RectDrawable windowPane = new RectDrawable(Color.argb(128, 32, 32, 32), Color.argb(192, 64, 64, 64), 2);
       windowPane.setBounds(drawRegions[DRAW_REGION_CENTER_BOUNDS]);
       layers.get(LAYER_OVERLAY).addDrawable(grOverlay);
       layers.get(LAYER_OVERLAY).addDrawable(closeText);
       layers.get(LAYER_OVERLAY).addGestureRegion(region);
       layers.get(LAYER_OVERLAY).addDrawable(windowPane);
        //overlayPaint.setShader(
        //        new RadialGradient(width / 2, height / 2, width / 4, Color.argb(128, 0, 0, 0), Color.argb(0, 0, 0, 0), Shader.TileMode.CLAMP));
        //layers.get(LAYER_OVERLAY).addRegion(new BorderedRegion(new RectF(0, 0, width, height), 0, overlayPaint, null));
    }

    public boolean openOverlay(BoardViewOverlay overlay, int overlayObjectId) {
        if (drawState == DrawState.NOTREADY) {
            return false;
        }
        addOverlayRegion();
        String bodyText = "Loading...";
        this.overlay = overlay;
        this.overlayObjectId = overlayObjectId;
        switch (overlay) {
        case NONE:
            return false;
        case PLAYER:
            bodyText = listener.getPlayerOverlayText(overlayObjectId);
            break;
        case ESTATE:
            bodyText = listener.getEstateOverlayText(overlayObjectId);
            break;
        case AUCTION:
            bodyText = listener.getAuctionOverlayText(overlayObjectId);
            break;
        case TRADE:
            bodyText = listener.getTradeOverlayText(overlayObjectId);
            break;
        }
        if (bodyText == null) {
            return false;
        }
        TextDrawable overlayBody = new TextDrawable(
                bodyText,
                getPixelSize(DPI_SIZE_TEXT),
                Color.LTGRAY, Color.LTGRAY,
                Alignment.ALIGN_NORMAL,
                VerticalAlignment.VALIGN_TOP);
        Rect bounds = new Rect(drawRegions[DRAW_REGION_CENTER_BOUNDS]);
        bounds.inset(12, 12);
        overlayBody.setBounds(new Rect(bounds.left, bounds.top, bounds.right, bounds.bottom));
        overlayBody.invalidateSelf();
        layers.get(LAYER_OVERLAY).addDrawable(overlayBody);
        bounds.top = bounds.bottom - (int) getPixelSize(DPI_BUTTON_HEIGHT);
        
        ArrayList<OverlayButton> buttons = new ArrayList<OverlayButton>();
        switch (overlay) {
        default:
            break;
        case NONE:
            return false;
        case PLAYER:
            buttons.addAll(listener.getPlayerOverlayButtons(overlayObjectId));
            break;
        case ESTATE:
            buttons.addAll(listener.getEstateOverlayButtons(overlayObjectId));
            break;
        case AUCTION:
            buttons.addAll(listener.getAuctionOverlayButtons(overlayObjectId));
            break;
        case TRADE:
            buttons.addAll(listener.getTradeOverlayButtons(overlayObjectId));
            break;
        }
        int index = 0;
        int w = bounds.right - bounds.left;
        for (OverlayButton button : buttons) {
            int width = button.getWidth() * w / 6;
            Rect btnBounds = new Rect(bounds.left + index, bounds.top, bounds.left + index + width, bounds.bottom);
            addButton(LAYER_OVERLAY, btnBounds, button.getCaption(), button.isEnabled(), TAG_OVERLAY_BUTTON + index, button.getListener());
            index += width;
        }
        hasChanges = true;
        return true;
    }

    public void addTurnRegions(ArrayList<Estate> estates, int[] playerIds, SparseArray<Player> players, ArrayList<Button> buttons, int selfPlayerId) {
        if (drawState == DrawState.NOTREADY) {
            return;
        }
        for (int playerId : playerIds) {
            if (playerId > 0) {
                final Player player = players.get(playerId); 
                if (player.isTurn()) {
                    Rect bounds = new Rect(drawRegions[DRAW_REGION_CENTER_BOUNDS]);
                    bounds.inset(width / 16 + 6, height / 16 + 6);
                    int lineHeight = (int) getPixelSize(DPI_LINE_HEIGHT);
                    int buttonHeight = (int) getPixelSize(DPI_BUTTON_HEIGHT);
                    Rect textBounds = new Rect(bounds.left, bounds.top, bounds.right, bounds.top + lineHeight * 3);
                    Rect btn1Bounds = new Rect(bounds.left, bounds.top + lineHeight * 3, bounds.right, bounds.top + lineHeight * 3 + buttonHeight);
                    Rect btn2Bounds = new Rect(bounds.left, bounds.top + lineHeight * 3 + buttonHeight, bounds.right, bounds.top + lineHeight * 3 + buttonHeight * 2);
                    Rect btn3Bounds = new Rect(bounds.left, bounds.top + lineHeight * 3 + buttonHeight * 2, bounds.right, bounds.top + lineHeight * 3 + buttonHeight * 3);
                    Estate estate = estates.get(player.getLocation());
                    String location = "On " + BoardActivity.makeEstateName(estate);
                    if (player.isJailed()) {
                        location = "In <b><font color=\"red\">Jail</font></b>";
                    }
                    
                    String actionText = "Current turn is " + BoardActivity.makePlayerName(player) + "<br>" + location + "<br>";
                    if (player.canRoll()) {
                        if (player.getPlayerId() == selfPlayerId) {
                            actionText += "Roll the dice:";
                        } else {
                            actionText += "Player may roll the dice.";
                        }
                    } else if (player.canBuyEstate()) {
                        if (player.getPlayerId() == selfPlayerId) {
                            actionText += "Buy estate for $" + estates.get(player.getLocation()).getPrice() + "?:";
                        } else {
                            actionText += "Player may buy the estate.";
                        }
                    } else if (buttons.size() > 0) {
                        if (player.getPlayerId() == selfPlayerId) {
                            actionText += "Choose an action:";
                        } else {
                            actionText += "Player may choose an action.";
                        }
                    } else if (player.isInDebt()) {
                        if (player.getPlayerId() == selfPlayerId) {
                            actionText += "You are in debt. You must pay it off by mortgaging properties or selling assets.";
                        } else {
                            actionText += "Player is in debt.";
                        }
                    }
                    TextDrawable turnHeader = new TextDrawable(
                            actionText,
                            getPixelSize(DPI_SIZE_TEXT),
                            Color.WHITE, Color.WHITE,
                            Alignment.ALIGN_NORMAL,
                            VerticalAlignment.VALIGN_TOP);
                    turnHeader.setBounds(textBounds);
                    layers.get(LAYER_TURN).addDrawable(turnHeader);

                    int index = 0;
                    for (final Button btn : buttons) {
                        Rect btnBounds = null;
                        int tag = 0;
                        switch (index) {
                        case 0:
                            btnBounds = btn1Bounds;
                            tag = TAG_TURN_BUTTON_1;
                            break;
                        case 1:
                            btnBounds = btn2Bounds;
                            tag = TAG_TURN_BUTTON_2;
                            break;
                        case 2:
                            btnBounds = btn3Bounds;
                            tag = TAG_TURN_BUTTON_3;
                            break;
                        default:
                            return;
                        }

                        addButton(
                                LAYER_TURN,
                                btnBounds,
                                btn.getCaption(),
                                btn.isEnabled(),
                                tag,
                                new GestureRegionListener() {
                                    @Override
                                    public void onGestureRegionClick(GestureRegion gestureRegion) {
                                        listener.onButtonCommand(btn.getCommand());
                                    }
                                });
                        
                        index++;
                    }
                    if (player.canRoll() && playerId == selfPlayerId) {
                        addButton(
                                LAYER_TURN,
                                btn1Bounds,
                                "Roll",
                                true,
                                TAG_TURN_BUTTON_1,
                                new GestureRegionListener() {
                                    @Override
                                    public void onGestureRegionClick(GestureRegion gestureRegion) {
                                        listener.onRoll();
                                    }
                                });
                    }
                    
                    /*else if (player.canBuyEstate()) {
                        boolean auctionButtonEnabled = player.canAuction();
                        
                        addButton(
                                LAYER_TURN,
                                btn1Bounds,
                                "Buy Estate",
                                true,
                                TAG_TURN_BUTTON_1,
                                new GestureRegionListener() {
                                    @Override
                                    public void onGestureRegionClick(GestureRegion gestureRegion) {
                                        thisListener.onBuyEstate();
                                    }
                                });
                        
                        addButton(
                                LAYER_TURN,
                                btn2Bounds,
                                "Auction Estate",
                                auctionButtonEnabled,
                                TAG_TURN_BUTTON_2,
                                new GestureRegionListener() {
                                    @Override
                                    public void onGestureRegionClick(GestureRegion gestureRegion) {
                                        thisListener.onAuctionEstate();
                                    }
                                });
                        
                        addButton(
                                LAYER_TURN,
                                btn3Bounds,
                                "End Turn",
                                !auctionButtonEnabled,
                                TAG_TURN_BUTTON_3,
                                new GestureRegionListener() {
                                    @Override
                                    public void onGestureRegionClick(GestureRegion gestureRegion) {
                                        thisListener.onEndTurn();
                                    }
                                });
                    }*/
                    break;
                }
            }
        }
    }

    private void addButton(int layerId, Rect bounds, String buttonText, boolean buttonEnabled, int gestureRegionTag,
            GestureRegionListener gestureRegionListener) {
        ButtonDrawable button = new ButtonDrawable(context);
        TextDrawable text = new TextDrawable(
                buttonText,
                getPixelSize(DPI_SIZE_TEXT),
                Color.WHITE, Color.DKGRAY,
                Alignment.ALIGN_CENTER,
                VerticalAlignment.VALIGN_MIDDLE);
        GestureRegion region = new GestureRegion(bounds, gestureRegionTag, gestureRegionListener); 
        button.setBounds(bounds);
        text.setBounds(bounds);
        region.addStateHandler(button);
        region.addStateHandler(text);
        if (buttonEnabled) {
            region.enable();
        } else {
            region.disable();
        }
        layers.get(layerId).addDrawable(button);
        layers.get(layerId).addDrawable(text);
        layers.get(layerId).addGestureRegion(region);
    }

    /*public static GradientDrawable createPieceGradient(int playerId) {
        GradientDrawable grad = pieceGradientCache.get(playerId);
        if (grad == null) {
            grad = new GradientDrawable(Orientation.LEFT_RIGHT, new int[] { color, darken(color) });
            grad.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            grad.setShape(GradientDrawable.OVAL);
            grad.setSize(25, 25);
            grad.setGradientRadius(grad.getIntrinsicWidth() / 2f);
            //grad.setGradientCenter(grad.getIntrinsicWidth() / 3f, grad.getIntrinsicHeight() / 3f);
            pieceGradientCache.put(color, grad);
        }
        return grad;
    }*/

    public static GradientDrawable createEstateGradient(int color, Orientation orientation) {
        GradientDrawable grad = new GradientDrawable(orientation, new int[] { color, darken(color) });
        grad.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        grad.setShape(GradientDrawable.RECTANGLE);
        grad.setSize(25, 25);
        //grad.setGradientCenter(grad.getIntrinsicWidth() / 3f, grad.getIntrinsicHeight() / 3f);
        //estateGradientCache.put(color, grad);
        return grad;
    }

    /**
     * Make a color darker by lowering its HSV value by 50% of full value.
     * @param color The input color.
     * @return The output color.
     */
    public static int darken(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] -= 0.5f;
        return Color.HSVToColor(hsv);
    }

    /**
     * Make a color more transparent by lowering its alpha to 75%.
     * @param color The solid color.
     * @return The output color.
     */
    public static int fade(int color) {
        return Color.argb(192, Color.red(color), Color.green(color), Color.blue(color));
    }

    public void waitDraw() {
        hasChanges = true;
        waitDraw = true;
        if (running) {
            while (waitDraw) {
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    waitDraw = false;
                    return;
                }
            }
        } else {
            waitDraw = false;
        }
    }
    
    private float getPixelSize(int dpiSize) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpiSize, context.getResources().getDisplayMetrics());
    }

    public BoardViewOverlay getOverlay() {
        return overlay;
    }

    public int getOverlayObjectId() {
        return overlayObjectId;
    }
}
