package edu.rochester.nbook.monopdroid.board.surface;

import java.util.ArrayList;
import java.util.List;

import edu.rochester.nbook.monopdroid.board.Configurable;
import edu.rochester.nbook.monopdroid.board.Estate;
import edu.rochester.nbook.monopdroid.board.GameStatus;
import edu.rochester.nbook.monopdroid.board.Player;
import edu.rochester.nbook.monopdroid.board.surface.BoardViewPiece.For;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.ShapeDrawable;
import android.os.Handler;
import android.text.Layout.Alignment;
import android.util.Log;
import android.util.SparseArray;
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
    private static final int TAG_OVERLAY_BUTTON_1 = 62; // unique
    private static final int TAG_OVERLAY_BUTTON_2 = 63; // unique
    private static final int TAG_OVERLAY_BUTTON_3 = 64; // unique
    private static final int TAG_OVERLAY_BUTTON_4 = 65; // unique
    private static final int TAG_OVERLAY_BUTTON_5 = 66; // unique
    private static final int TAG_OVERLAY_BUTTON_6 = 67; // unique
    
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
    private static final int DRAW_POINT_BOARD_ESTATE_PIECE_POSITION = 0;
    private static final int DRAW_POINT_BOARD_ESTATE_PIECE_OFFSET = 40;
    private static final int DRAW_POINT_BOARD_ESTATE_HOUSE_POSITION = 80;
    private static final int DRAW_POINT_BOARD_ESTATE_HOUSE_OFFSET = 120;
    private static final int DRAW_POINT_BOARD_ESTATE_PIECE_RADIUS = 160;
    private static final int DRAW_POINT_BOARD_ESTATE_HOUSE_RADIUS = 161;
    private static final int DRAW_POINT_BOARD_COUNT = 162;

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
    
    // has cached data been initialized yet?
    private static boolean staticInit = false;
    
    // draw thread cached images
    //private static SparseArray<GradientDrawable> estateGradientCache = new SparseArray<GradientDrawable>();
    
    private static Path housePath = new Path();
    {
        housePath.moveTo(0, 4);
        housePath.lineTo(2, 4);
        housePath.lineTo(2, 1);
        housePath.lineTo(1, 0);
        housePath.lineTo(0, 1);
        housePath.close();
    }
    
    private enum OverlayState {
        NONE, OVERLAY,
        OVERLAY_PLAYER, OVERLAY_ESTATE, OVERLAY_AUCTION, OVERLAY_TRADE
    }
    
    private OverlayState overlay = OverlayState.NONE;
    private TextDrawable overlayBody = null;
    private Object overlayObject = null;
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
    
    private ArrayList<String> configIndexMap = null; 
    
    // whether we are in the foreground
    private boolean running = false;
    // game status
    private GameStatus status = GameStatus.CREATE;
    // layers init
    private List<DrawLayer> layers = new ArrayList<DrawLayer>() {
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
    
    public static final int animationSteps = 10;
    
    private SurfaceHolder surfaceHolder = null;
    private BoardViewListener thisListener = null;
    
    public void setListener(BoardViewListener listener) {
        thisListener = listener;
    }

    public BoardViewListener getListener() {
        return thisListener;
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
            if (this.surfaceHolder.getSurface().isValid()) {
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
                waitDraw = false;
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
        if (overlay != OverlayState.NONE) {
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
    }
    
    private void commitPZState(PZState state) {
        pzAnimRunnable = null;
        pzState = state;
    }
    
    private int pzAnimIndex = 0;
    private Runnable pzAnimRunnable = null;
    
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
        if (overlay != OverlayState.NONE) {
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
        if (overlay != OverlayState.NONE) {
            return;
        }
        float offsetX = pzState.getOffsetX();
        float offsetY = pzState.getOffsetY();
        float scale = pzState.getScale();
        offsetX -= centerX / scale;
        offsetY -= centerY / scale;
        scale *= scaleFactor;
        if (scale < 1f) {
            scale = 1f;
        } else if (scale > 4f) {
            scale = 4f;
        }
        offsetX -= -centerX / scale;
        offsetY -= -centerY / scale;
        PZState state = new PZState(offsetX, offsetY, scale);
        state.snapBounds();
        commitPZState(state);
    }
    
    private void zoomOut() {
        PZState state = new PZState();
        animateToPZState(state, 15, 30);
    }
    
    private void zoomIn(float centerX, float centerY) {
        float offsetX = pzState.getOffsetX();
        float offsetY = pzState.getOffsetY();
        float scale = pzState.getScale();
        offsetX -= centerX / scale;
        offsetY -= centerY / scale;
        scale = 4f;
        offsetX -= -centerX / scale;
        offsetY -= -centerY / scale;
        PZState state = new PZState(offsetX, offsetY, scale);
        state.snapBounds();
        animateToPZState(state, 15, 30);
    }

    public void onDoubleTap(float x, float y) {
        if (overlay != OverlayState.NONE) {
            return;
        }
        if (pzState.scale == 1f) {
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
            if (overlay == OverlayState.NONE || layer.getIndex() == LAYER_OVERLAY) {
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
    }

    public void onShowPressUp(int x, int y) {
        findRegionIntersect(x, y, new GestureRegionAction() {
            @Override
            public boolean onGestureRegionIntersect(GestureRegion region) {
                region.up();
                return true;
            }
        });
    }

    public void commitRegions(int layer) {
        layers.get(layer).commitRegions();
    }

    public void beginRegions(int layer) {
        layers.get(layer).beginRegions();
    }

    public void clearRegions(int layer) {
        layers.get(layer).clearRegions();
    }

    public void setSize(int width, int height) {
        if (this.width != width && this.height != height) {
            this.width = width;
            this.height = height;
            
            thisListener.onResize(this.getWidth(), this.getHeight());
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<DrawLayer> getRegions() {
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
                new Rect(width / 16, height / 16, width * 15 / 16, height * 15 / 16);
                //new Rect((int)(2f * phi * part), (int)(2f * phi * part), (int)(width - 2f * phi * part), (int)(height - 2f * phi * part));
    }
    
    public void calculateConfigRegions() {
        if (width == 0 || height == 0) {
            return;
        }
        drawState = DrawState.CONFIG;
        drawRegions = new Rect[DRAW_REGION_CONFIG_COUNT];
        drawPoints = new Point[DRAW_POINT_CONFIG_COUNT];
        
        drawRegions[DRAW_REGION_CONFIG_BUTTON_BOUNDS] =
                new Rect(60, height - 80, width - 60, height - 5);
                //new Rect(60, height - 38, width - 60, height - 5);
        for (int index = 0; index < 16; index++) {
            drawRegions[DRAW_REGION_CONFIG_CHECK_BOX_BOUNDS + index] =
                    new Rect(5, 10 + (index * 55), 40, 55 + (index * 55));
            drawRegions[DRAW_REGION_CONFIG_CHECK_TEXT_BOUNDS + index] =
                    new Rect(45, 10 + (index * 55), width, 55 + (index * 55));
                    //new Rect(45, 32 + (index * 55), width, 55 + (index * 55));
        }
        //float part = ((float)width / ((4f * phi) + 18f));
        drawRegions[DRAW_REGION_CENTER_BOUNDS] =
                new Rect(width / 16, height / 16, width * 15 / 16, height * 15 / 16);
                //new Rect((int)(2f * phi * part), (int)(2f * phi * part), (int)(width - 2f * phi * part), (int)(height - 2f * phi * part));
    }

    private boolean range(int value, int min, int max) {
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
                iconY = ((4f * phi) + 18f) - 1.4f;
                pieceY = ((4f * phi) + 18f) - 0.7f;
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
                iconX = 1.4f;
                pieceX = 0.7f;
                pieceY = iconY = estateY + 1f;
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
                iconY = 1.4f;
                pieceY = 0.7f;
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
                    iconX = ((4f * phi) + 18f) - 1.4f;
                    pieceX = ((4f * phi) + 18f) - 0.7f;
                    pieceY = iconY = estateY + 1f;
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
            drawPoints[DRAW_POINT_BOARD_ESTATE_PIECE_POSITION + index] =
                    new Point((int)pieceX, (int)pieceY);
            drawPoints[DRAW_POINT_BOARD_ESTATE_PIECE_OFFSET + index] =
                    new Point((int)pieceDeltaX, (int)pieceDeltaY);
            drawPoints[DRAW_POINT_BOARD_ESTATE_HOUSE_POSITION + index] =
                    new Point((int)pieceX, (int)pieceY);
            drawPoints[DRAW_POINT_BOARD_ESTATE_HOUSE_OFFSET + index] =
                    new Point((int)pieceDeltaX, (int)pieceDeltaY);
        }
        drawPoints[DRAW_POINT_BOARD_ESTATE_HOUSE_RADIUS] =
                new Point((int)radius, (int)radius);
        drawPoints[DRAW_POINT_BOARD_ESTATE_PIECE_RADIUS] =
                new Point((int)radius, (int)radius);
        drawRegions[DRAW_REGION_CENTER_BOUNDS] =
                new Rect(width / 16, height / 16, width * 15 / 16, height * 15 / 16);
                //new Rect((int)(2f * phi * part), (int)(2f * phi * part), (int)(width - 2f * phi * part), (int)(height - 2f * phi * part));
    }

    public void createTextRegion(String string, boolean isError) {
        if (drawState == DrawState.NOTREADY) {
            return;
        }
        TextDrawable text = new TextDrawable(
                string,
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
    
    public void addConfigurableRegions(final List<Configurable> configurables) {
        if (drawState == DrawState.NOTREADY) {
            return;
        }
        int index = 0;
        configIndexMap = new ArrayList<String>();
        for (final Configurable config : configurables) {
            configIndexMap.add(config.getCommand());
            Rect encl = new Rect(drawRegions[DRAW_REGION_CONFIG_CHECK_BOX_BOUNDS + index]);
            encl.union(drawRegions[DRAW_REGION_CONFIG_CHECK_TEXT_BOUNDS + index]);
            CheckboxDrawable checkbox = new CheckboxDrawable(context);
            checkbox.setBounds(drawRegions[DRAW_REGION_CONFIG_CHECK_BOX_BOUNDS + index]);
            TextDrawable text = new TextDrawable(
                    config.getTitle(),
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
                        public void onGestureRegionLongPress(GestureRegion region) {
                            // do nothing on check box long-press
                        }
    
                        @Override
                        public void onGestureRegionClick(GestureRegion region) {
                            if (thisListener != null) {
                                for (Configurable currentConfigurable : configurables) {
                                    if (currentConfigurable.getCommand().equals(config.getCommand())) {
                                        thisListener.onConfigChange(currentConfigurable.getCommand(),
                                                currentConfigurable.getValue().equals("0") ? "1" : "0");
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

    public void updateConfigurableRegions(List<Configurable> configurables) {
        for (Configurable config : configurables) {
            int configIndex = configIndexMap.indexOf(config.getCommand());
            if (configIndex >= 0) {
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
            }
        }
    }

    public void addStartButtonRegions(boolean isMaster) {
        if (drawState == DrawState.NOTREADY) {
            return;
        }
        ButtonDrawable button = new ButtonDrawable(context);
        TextDrawable text = new TextDrawable(
                "Start Game",
                Color.WHITE,
                Color.DKGRAY,
                Alignment.ALIGN_CENTER,
                VerticalAlignment.VALIGN_MIDDLE);
        GestureRegion item = new GestureRegion(
                drawRegions[DRAW_REGION_CONFIG_BUTTON_BOUNDS],
                TAG_GAME_START_BUTTON,
                new GestureRegionListener() {
                    @Override
                    public void onGestureRegionLongPress(GestureRegion region) {
                        // do nothing on start button long press
                    }
        
                    @Override
                    public void onGestureRegionClick(GestureRegion region) {
                        thisListener.onStartGame();
                    }
                });
        button.setBounds(drawRegions[DRAW_REGION_CONFIG_BUTTON_BOUNDS]);
        text.setBounds(drawRegions[DRAW_REGION_CONFIG_BUTTON_BOUNDS]);
        item.addStateHandler(button);
        item.addStateHandler(text);
        if (isMaster) {
            item.enable();
        } else {
            item.disable();
        }
        
        layers.get(LAYER_BACKGROUND).addDrawable(button);
        layers.get(LAYER_BACKGROUND).addDrawable(text);
        layers.get(LAYER_BACKGROUND).addGestureRegion(item);
    }

    public void updateStartButtonRegions(boolean isMaster) {
        GestureRegion region = (GestureRegion) layers.get(LAYER_BACKGROUND).getGestureRegion(TAG_GAME_START_BUTTON);
        if (isMaster) {
            region.enable();
        } else {
            region.disable();
        }
    }

    public void addEstateRegions(ArrayList<Estate> estates, SparseArray<Player> players) {
        if (drawState == DrawState.NOTREADY) {
            return;
        }
        for (int index = 0; index < 40; index++) {
            final int estateId = index;
            Estate estate = estates.get(index);
            Rect bounds = new Rect(drawRegions[DRAW_REGION_BOARD_ESTATE_BOUNDS + index]);
            GestureRegion region = new GestureRegion(
                    drawRegions[DRAW_REGION_BOARD_ESTATE_BOUNDS + index],
                    TAG_ESTATE + index,
                    new GestureRegionListener() {
                        @Override
                        public void onGestureRegionLongPress(GestureRegion region) {
                            // TODO: on estate long press
                        }
                        
                        @Override
                        public void onGestureRegionClick(GestureRegion gestureRegion) {
                            thisListener.onEstateClick(estateId);
                        }
                    });
            RectDrawable estateDraw = new RectDrawable(estate.getBgColor(), darken(estate.getBgColor()), Color.BLACK, 2);
            estateDraw.setBounds(new Rect(bounds));
            region.addStateHandler(estateDraw);
            region.enable();
            layers.get(LAYER_BACKGROUND).addGestureRegion(region);
            layers.get(LAYER_BACKGROUND).addDrawable(estateDraw);
            
            if (estate.getColor() != 0) {
                Orientation gradOrient = Orientation.LEFT_RIGHT;
                Point pieceOffset = drawPoints[DRAW_POINT_BOARD_ESTATE_PIECE_OFFSET + index];
                if (pieceOffset.x > 0 && pieceOffset.y == 0) {
                    // 0 to 10
                    gradOrient = Orientation.RIGHT_LEFT;
                } else if (pieceOffset.x == 0 && pieceOffset.y > 0) {
                    // 10 to 20
                    gradOrient = Orientation.BOTTOM_TOP;
                } else if (pieceOffset.x < 0 && pieceOffset.y == 0) {
                    // 20 to 30
                    gradOrient = Orientation.LEFT_RIGHT;
                } else {
                    // 30 to 40
                    gradOrient = Orientation.TOP_BOTTOM;
                }
                GradientDrawable grad = createEstateGradient(estate.getColor(), gradOrient);
                RectDrawable estateGradientDraw = new RectDrawable(grad, Color.BLACK, 2);
                estateGradientDraw.setBounds(drawRegions[DRAW_REGION_BOARD_ESTATE_GRAD_BOUNDS + index]);
                layers.get(LAYER_BACKGROUND).addDrawable(estateGradientDraw);
                int color = Color.GREEN;
                int houseCount = estate.getHouses();
                if (houseCount == 5) {
                    houseCount = 1;
                    color = Color.RED;
                }
                Point offset = drawPoints[DRAW_POINT_BOARD_ESTATE_HOUSE_OFFSET + index];
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
                }
            }
            
            if (estate.getOwner() > 0) {
                int ownerColor = 0;
                int borderWidth = 4;
                int insetWidth = 3;
                BoardViewPiece piece = BoardViewPiece.getPiece(estate.getOwner());
                if (piece != null) {
                    ownerColor = piece.getColor();
                    if (estate.isMortgaged()) {
                        ownerColor = Color.argb(
                                128,
                                Color.red(ownerColor),
                                Color.green(ownerColor),
                                Color.blue(ownerColor));
                        borderWidth = 4;
                        insetWidth = 3;
                    }
                }
                RectDrawable estateOwnerBorder = new RectDrawable(0, ownerColor, borderWidth);
                bounds.inset(insetWidth, insetWidth);
                estateOwnerBorder.setBounds(bounds);
                layers.get(LAYER_BACKGROUND).addDrawable(estateOwnerBorder);
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
                    for (int j = 0; j < 4; j++) {
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
        for (int i = 0; i < 4; i++) {
            int playerId = BoardViewPiece.pieces[i].getPlayerId();
            int currentEstate = BoardViewPiece.pieces[i].getCurrentEstate();
            int progressEstate = BoardViewPiece.pieces[i].getProgressEstate();
            int progressEstateDelta = BoardViewPiece.pieces[i].getProgressEstateDelta();
            // location of piece = PROGRESSDELTA steps / animation steps, between PROGRESS and CURRENT
            if (playerId > 0) {
                int sameEstateIndex = 0;
                int sameEstate = 0;
                for (int j = 0; j < 4; j++) {
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
                Point offsetP = drawPoints[DRAW_POINT_BOARD_ESTATE_PIECE_OFFSET + progressEstate];
                float radius = drawPoints[DRAW_POINT_BOARD_ESTATE_PIECE_RADIUS].x;
                float offsetScale = sameEstateIndex - (sameEstate) / 2f;
                Point locationP = new Point(drawPoints[DRAW_POINT_BOARD_ESTATE_PIECE_POSITION + progressEstate]);
                Point locationC = new Point(drawPoints[DRAW_POINT_BOARD_ESTATE_PIECE_POSITION + currentEstate]);
                if (locationP.equals(locationC)) {
                    locationP.offset((int)(offsetP.x * offsetScale), (int)(offsetP.y * offsetScale));
                } else {
                    float progress = (float)progressEstateDelta / (float)animationSteps; 
                    locationC.offset(-locationP.x, -locationP.y);
                    locationC = new Point((int)(locationC.x * progress), (int)(locationC.y * progress));
                    locationC.offset(locationP.x, locationP.y);
                    locationP = new Point(locationC.x, locationC.y);
                }
                Rect rect = new Rect((int)locationP.x - (int)radius, (int)locationP.y - (int)radius, (int)locationP.x + (int)radius, (int)locationP.y + (int)radius);
                Drawable draw = BoardViewPiece.pieces[i].getDrawable(For.BOARD);
                draw.setBounds(rect);
                layers.get(LAYER_PIECES).addDrawable(draw);
            }
        }
    }

    public void addOverlayRegion() {
        if (drawState == DrawState.NOTREADY) {
            return;
        }

        if (currentEstateRegion != null) {
            currentEstateRegion.unfocus();
            currentEstateRegion = null;
        }
        
        overlay = OverlayState.OVERLAY;
        layers.get(LAYER_OVERLAY).setVisible(true);
        
        GradientDrawable grOverlay = new GradientDrawable(Orientation.LEFT_RIGHT, new int[] { Color.argb(224, 0, 0, 0), Color.argb(192, 0, 0, 0) });
        grOverlay.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        //grOverlay.setGradientCenter(width / 2, height / 2);
        grOverlay.setShape(GradientDrawable.RECTANGLE);
        grOverlay.setSize(width, height);
        grOverlay.setGradientRadius(width / 2f);
        grOverlay.setBounds(0, 0, width, height);
        layers.get(LAYER_OVERLAY).addDrawable(grOverlay);
        GestureRegion region = new GestureRegion(
                new Rect(0, 0, width, height),
                TAG_OVERLAY,
                new GestureRegionListener() {
                    @Override
                    public void onGestureRegionLongPress(GestureRegion region) {
                        // do nothing
                    }
                    
                    @Override
                    public void onGestureRegionClick(GestureRegion gestureRegion) {
                        thisListener.onCloseOverlay();
                        overlay = OverlayState.NONE;
                        layers.get(LAYER_OVERLAY).setVisible(false);
                        if (currentEstateRegion != null) {
                            currentEstateRegion.unfocus();
                            currentEstateRegion = null;
                        }
                    }
                });
       layers.get(LAYER_OVERLAY).addGestureRegion(region);
       RectDrawable windowPane = new RectDrawable(Color.argb(128, 32, 32, 32), Color.argb(192, 64, 64, 64), 2);
       windowPane.setBounds(drawRegions[DRAW_REGION_CENTER_BOUNDS]);
       layers.get(LAYER_OVERLAY).addDrawable(windowPane);
        //overlayPaint.setShader(
        //        new RadialGradient(width / 2, height / 2, width / 4, Color.argb(128, 0, 0, 0), Color.argb(0, 0, 0, 0), Shader.TileMode.CLAMP));
        //layers.get(LAYER_OVERLAY).addRegion(new BorderedRegion(new RectF(0, 0, width, height), 0, overlayPaint, null));
    }

    public void updateOverlay() {
        String bodyText = "Loading...";
        switch (overlay) {
        default:
            break;
        case NONE:
            return;
        case OVERLAY_PLAYER:
            Player player = (Player) overlayObject;
            bodyText = thisListener.getPlayerBodyText(player);
            break;
        case OVERLAY_ESTATE:
            Estate estate = (Estate) overlayObject;
            bodyText = thisListener.getEstateBodyText(estate);
            break;
        }
        overlayBody = new TextDrawable(
                bodyText,
                Color.LTGRAY, Color.LTGRAY,
                Alignment.ALIGN_NORMAL,
                VerticalAlignment.VALIGN_TOP);
        Rect bounds = new Rect(drawRegions[DRAW_REGION_CENTER_BOUNDS]);
        bounds.inset(6, 6);
        overlayBody.setBounds(new Rect(bounds.left, bounds.top, bounds.right, bounds.bottom));
        overlayBody.invalidateSelf();
    }

    public void addPlayerOverlayRegions(final Player player) {
        if (drawState == DrawState.NOTREADY) {
            return;
        }
        overlay = OverlayState.OVERLAY_PLAYER;
        overlayObject = player;
        updateOverlay();
        layers.get(LAYER_OVERLAY).addDrawable(overlayBody);

        Rect bounds = new Rect(drawRegions[DRAW_REGION_CENTER_BOUNDS]);
        bounds.inset(12, 12);
        bounds.top = bounds.bottom - 75;
        
        Rect tradeBounds = new Rect(bounds.left, bounds.top, bounds.right - bounds.width() / 2, bounds.bottom);
        addButton(LAYER_OVERLAY, tradeBounds, "Trade", status == GameStatus.RUN, TAG_OVERLAY_BUTTON_1, new GestureRegionListener() {
            @Override
            public void onGestureRegionLongPress(GestureRegion region) {}
            
            @Override
            public void onGestureRegionClick(GestureRegion gestureRegion) {
                thisListener.onOpenTradeWindow(player);
            }
        });
        
        Rect commandPingBounds = new Rect(bounds.left + bounds.width() / 2, bounds.top, bounds.left + (bounds.width() * 2) / 3, bounds.bottom);
        addButton(LAYER_OVERLAY, commandPingBounds, "Ping!", true, TAG_OVERLAY_BUTTON_2, new GestureRegionListener() {
            @Override
            public void onGestureRegionLongPress(GestureRegion region) {}
            
            @Override
            public void onGestureRegionClick(GestureRegion gestureRegion) {
                thisListener.onPlayerCommandPing(player);
            }
        });
        
        Rect commandDateBounds = new Rect(bounds.left + (bounds.width() * 2) / 3, bounds.top, bounds.left + (bounds.width() * 5) / 6, bounds.bottom);
        addButton(LAYER_OVERLAY, commandDateBounds, "Date?", true, TAG_OVERLAY_BUTTON_3, new GestureRegionListener() {
            @Override
            public void onGestureRegionLongPress(GestureRegion region) {}
            
            @Override
            public void onGestureRegionClick(GestureRegion gestureRegion) {
                thisListener.onPlayerCommandDate(player);
            }
        });
        
        Rect commandVerBounds = new Rect(bounds.left + (bounds.width() * 5) / 6, bounds.top, bounds.right, bounds.bottom);
        addButton(LAYER_OVERLAY, commandVerBounds, "Ver?", true, TAG_OVERLAY_BUTTON_4, new GestureRegionListener() {
            @Override
            public void onGestureRegionLongPress(GestureRegion region) {}
            
            @Override
            public void onGestureRegionClick(GestureRegion gestureRegion) {
                thisListener.onPlayerCommandVersion(player);
            }
        });
    }

    private void addButton(int layerId, Rect bounds, String buttonText, boolean buttonEnabled, int gestureRegionTag,
            GestureRegionListener gestureRegionListener) {
        ButtonDrawable button = new ButtonDrawable(context);
        TextDrawable text = new TextDrawable(
                buttonText,
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

    public void addEstateOverlayRegions(Estate estate) {
        if (drawState == DrawState.NOTREADY) {
            return;
        }
        currentEstateRegion = layers.get(LAYER_BACKGROUND).getGestureRegion(TAG_ESTATE + estate.getEstateId());
        currentEstateRegion.focus();
        overlay = OverlayState.OVERLAY_ESTATE;
        overlayObject = estate;
        updateOverlay();
        layers.get(LAYER_OVERLAY).addDrawable(overlayBody);
    }

    public void addTurnRegions(ArrayList<Estate> estates, int[] playerIds, SparseArray<Player> players) {
        if (drawState == DrawState.NOTREADY) {
            return;
        }
        for (int playerId : playerIds) {
            if (playerId > 0) {
                final Player player = players.get(playerId);
                Rect bounds = new Rect(drawRegions[DRAW_REGION_CENTER_BOUNDS]);
                bounds.inset(width / 16 + 6, height / 16 + 6);
                Rect textBounds = new Rect(bounds.left, bounds.top, bounds.right, bounds.top + 35);
                Rect textLn2Bounds = new Rect(bounds.left, bounds.top + 35, bounds.right, bounds.top + 70);
                Rect btn1Bounds = new Rect(bounds.left, bounds.top + 70, bounds.right, bounds.top + 145);
                Rect btn2Bounds = new Rect(bounds.left, bounds.top + 150, bounds.right, bounds.top + 225);
                Rect btn3Bounds = new Rect(bounds.left, bounds.top + 230, bounds.right, bounds.top + 305); 
                if (player.isTurn()) {
                    TextDrawable turnHeader = new TextDrawable(
                            "<b>It is " + player.getName() + "'s turn.</b>",
                            Color.WHITE, Color.WHITE,
                            Alignment.ALIGN_NORMAL,
                            VerticalAlignment.VALIGN_TOP, true);
                    turnHeader.setBounds(textBounds);
                    layers.get(LAYER_TURN).addDrawable(turnHeader);
                }
                if (player.canRoll()) {
                    TextDrawable actionHeader = new TextDrawable(
                            "Roll the dice:",
                            Color.LTGRAY, Color.LTGRAY,
                            Alignment.ALIGN_NORMAL,
                            VerticalAlignment.VALIGN_TOP, true);
                    actionHeader.setBounds(textLn2Bounds);
                    layers.get(LAYER_TURN).addDrawable(actionHeader);
                    
                    addButton(
                            LAYER_TURN,
                            btn1Bounds,
                            "Roll",
                            true,
                            TAG_TURN_BUTTON_1,
                            new GestureRegionListener() {
                                
                                @Override
                                public void onGestureRegionLongPress(GestureRegion region) { }
                                
                                @Override
                                public void onGestureRegionClick(GestureRegion gestureRegion) {
                                    thisListener.onRoll();
                                }
                            });
                } else if (player.canBuyEstate()) {
                    TextDrawable actionHeader = new TextDrawable(
                            "Buy " + estates.get(player.getLocation()).getName() + " for " + estates.get(player.getLocation()).getPrice() + "?:",
                            Color.LTGRAY, Color.LTGRAY,
                            Alignment.ALIGN_NORMAL,
                            VerticalAlignment.VALIGN_TOP, true);
                    actionHeader.setBounds(textLn2Bounds);
                    layers.get(LAYER_TURN).addDrawable(actionHeader);
                    
                    boolean auctionButtonEnabled = player.canAuction();
                    
                    addButton(
                            LAYER_TURN,
                            btn1Bounds,
                            "Buy Estate",
                            true,
                            TAG_TURN_BUTTON_1,
                            new GestureRegionListener() {
                                
                                @Override
                                public void onGestureRegionLongPress(GestureRegion region) { }
                                
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
                                public void onGestureRegionLongPress(GestureRegion region) { }
                                
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
                                public void onGestureRegionLongPress(GestureRegion region) { }
                                
                                @Override
                                public void onGestureRegionClick(GestureRegion gestureRegion) {
                                    thisListener.onEndTurn();
                                }
                            });
                }
            }
        }
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
     * Make a color darker by lowering its HSV value by 40% of full value.
     * @param color The input color.
     * @return The output color.
     */
    public static int darken(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] -= 0.4;
        return Color.HSVToColor(hsv);
    }

    public boolean isOverlayOpen() {
        return overlay != OverlayState.NONE;
    }
    
    private volatile boolean waitDraw = false;

    public void waitDraw() {
        waitDraw = true;
        while (waitDraw) {
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
                waitDraw = false;
                return;
            }
        }
    }
}
