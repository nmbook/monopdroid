package edu.rochester.nbook.monopdroid;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.text.Layout.Alignment;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;

public class BoardViewSurfaceThread implements Runnable {
    public static final int TAG_START_BUTTON = 10;
    public static final int TAG_CONFIG_TOGGLE = 11;
    public static final int TAG_ESTATE = 20;
    public static final int TAG_ROLL_BUTTON = 30;
    public static final int TAG_ESTATE_BUY_BUTTON = 31;
    public static final int TAG_ESTATE_AUCTION_BUTTON = 32;
    public static final int TAG_ESTATE_ENDTURN_BUTTON = 33;
    public static final int TAG_JAIL_BUY_BUTTON = 34;
    public static final int TAG_JAIL_CARD_BUTTON = 35;
    public static final int TAG_JAIL_ROLL_BUTTON = 36;
    public static final int TAG_TAX_200_BUTTON = 37;
    public static final int TAG_TAX_10P_BUTTON = 38;
    public static final int TAG_OVERLAY_HIDE_BUTTON = 30;
    public static final int TAG_OVERLAY_PLAYER_TRADE_BUTTON = 31;
    public static final int TAG_OVERLAY_PLAYER_REQUEST_VERSION_BUTTON = 32;
    public static final int TAG_OVERLAY_PLAYER_REQUEST_PING_BUTTON = 33;
    public static final int TAG_OVERLAY_PLAYER_REQUEST_TIME_BUTTON = 34;
    public static final int TAG_OVERLAY_ESTATE_BUYHOUSE_BUTTON = 35;
    public static final int TAG_OVERLAY_ESTATE_SELLHOUSE_BUTTON = 36;
    public static final int TAG_OVERLAY_ESTATE_MORTGAGE_BUTTON = 37;
    
    public static final int LAYER_BACKGROUND = 0;
    public static final int LAYER_OWNERS = 1;
    public static final int LAYER_PIECES = 2;
    public static final int LAYER_OVERLAY = 3;

    private static final float phi = 1.618033988749894f;
    
    private static boolean staticInit = false;
    
    // draw thread cached images
    public static Drawable checkIconChecked = null;
    public static Drawable checkIconUnchecked = null;
    public static Drawable checkIconCheckedDisabled = null;
    public static Drawable checkIconUncheckedDisabled = null;
    public static Drawable buttonEnabled = null;
    public static Drawable buttonDisabled = null;
    public static SparseArray<GradientDrawable> gradCache = new SparseArray<GradientDrawable>();
    
    // draw thread exclusives (cached data and scale state)
    public static Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static Paint textNegativePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static Paint estateBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static Paint estateBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static Paint playerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    // used to store draw state across threads
    private boolean running = false;
    private GameStatus status = GameStatus.CREATE;
    private List<RegionLayer> layers = new ArrayList<RegionLayer>() {
        private static final long serialVersionUID = 7072660638043030076L;

        {
            add(new RegionLayer(LAYER_BACKGROUND, false, true));
            add(new RegionLayer(LAYER_OWNERS, false, true));
            add(new RegionLayer(LAYER_PIECES, false, true));
            add(new RegionLayer(LAYER_OVERLAY, true, false));
        }
    };
    private float offsetX = 0f;
    private float offsetY = 0f;
    private float scale = 1f;
    // private Rect offsetBounds = new Rect();
    // private float maxScale = 1f;
    // private float minScale = 1f;
    private boolean fixed = true;
    
    // width and height of the internal data
    private int width = 0;
    private int height = 0;
    
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
        if (staticInit) {
            return;
        } else {
            staticInit = true;
        }
        checkIconChecked = context.getResources().getDrawable(R.drawable.btn_check_on);
        checkIconUnchecked = context.getResources().getDrawable(R.drawable.btn_check_off);
        checkIconCheckedDisabled = context.getResources().getDrawable(R.drawable.btn_check_on_disable);
        checkIconUncheckedDisabled = context.getResources().getDrawable(R.drawable.btn_check_off_disable);
        buttonEnabled = context.getResources().getDrawable(R.drawable.btn_default_normal);
        buttonDisabled = context.getResources().getDrawable(R.drawable.btn_default_normal_disable);
        bgPaint.setStyle(Style.FILL);
        bgPaint.setColor(Color.BLACK);
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
        playerPaint.setStyle(Style.FILL);
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
                for (RegionLayer layer : this.layers) {
                    canvas.save();
                    if (!this.fixed && !layer.isFixed()) {
                        canvas.scale(this.scale, this.scale);
                        canvas.translate(this.offsetX, this.offsetY);
                    }
                    layer.drawRegions(canvas);
                    canvas.restore();
                }
                this.surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public boolean isFixed() {
        return fixed;
    }
    
    public void doTranslate(float distanceX, float distanceY) {
        this.offsetX -= distanceX;
        /*if (this.offsetX < this.offsetBounds.left) {
            this.offsetX = this.offsetBounds.left;
        }
        if (this.offsetX > this.offsetBounds.right) {
            this.offsetX = this.offsetBounds.right;
        }*/
        this.offsetY -= distanceY;
        /*if (this.offsetY < this.offsetBounds.top) {
            this.offsetY = this.offsetBounds.top;
        }
        if (this.offsetY < this.offsetBounds.bottom) {
            this.offsetY = this.offsetBounds.bottom;
        }*/
    }

    public List<RegionLayer> getRegions() {
        return this.layers;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
        this.offsetX = this.offsetY = 0f;
        this.scale = 1f;
        this.fixed = status != GameStatus.RUN;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    private boolean range(int value, int min, int max) {
        return (value >= min && value <= max);
    }

    public void scale(float scaleFactor) {
        this.scale *= scaleFactor;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

    public void commitRegions(int layer) {
        layers.get(layer).commitRegions();
    }

    public void beginRegions(int layer) {
        layers.get(layer).beginRegions();
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Check intersections with enabled layers.
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return Whether the single tap succeeded.
     */
    public boolean onSingleTapUp(int x, int y) {
        for (RegionLayer layer : layers) {
            for (Region region : layer.getRegions()) {
                if (region instanceof GestureRegion) {
                    GestureRegion greg = (GestureRegion) region;
                    if (greg.isEnabled() && greg.getBounds().contains(x, y)) {
                        greg.invokeClick();
                        greg.onUp();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void onShowPress(int x, int y) {
        for (RegionLayer layer : layers) {
            for (Region region : layer.getRegions()) {
                if (region instanceof GestureRegion) {
                    GestureRegion greg = (GestureRegion) region;
                    if (greg.isEnabled() && greg.getBounds().contains(x, y)) {
                        greg.onDown();
                        return;
                    }
                }
            }
        }
    }

    public void onLongPress(int x, int y) {
        for (RegionLayer layer : layers) {
            for (Region region : layer.getRegions()) {
                if (region instanceof GestureRegion) {
                    GestureRegion greg = (GestureRegion) region;
                    if (greg.isEnabled() && greg.getBounds().contains(x, y)) {
                        greg.invokeLongPress();
                        greg.onUp();
                        return;
                    }
                }
            }
        }
    }

    public void createTextRegion(String string) {
        if (width == 0 || height == 0) {
            return;
        }
        layers.get(LAYER_BACKGROUND).addRegion(new TextRegion(new RectF(0, 20,
                width, height), 0, string, textPaint, Alignment.ALIGN_NORMAL));
    }
    
    public void addConfigurableRegions(final List<Configurable> configurables) {
        if (width == 0 || height == 0) {
            return;
        }
        int index = 0;
        for (final Configurable config : configurables) {
            Drawable check = null;
            if (config.isEditable()) {
                if (config.getValue().equals("0")) {
                    check = checkIconUnchecked;
                } else {
                    check = checkIconChecked;
                }
                layers.get(LAYER_BACKGROUND).addRegion(new GestureRegion(new RectF(0, 5 + (index * 55), this
                        .width, 60 + (index * 55)), TAG_CONFIG_TOGGLE, highlightPaint, new GestureRegionListener() {

                    @Override
                    public void onRegionLongPress(Region region) {
                        // do nothing on check box long-press
                    }

                    @Override
                    public void onRegionClick(Region region) {
                        if (thisListener != null) {
                            for (Configurable currentConfigurable : configurables) {
                                if (currentConfigurable.getCommand().equals(config.getCommand())) {
                                    thisListener.onConfigChange(currentConfigurable.getCommand(),
                                            currentConfigurable.getValue().equals("0") ? "1" : "0");
                                }
                            }
                        }
                    }
                }));
            } else {
                if (config.getValue().equals("0")) {
                    check = checkIconUncheckedDisabled;
                } else {
                    check = checkIconCheckedDisabled;
                }
            }
            // draw image
            layers.get(LAYER_BACKGROUND).addRegion(new DrawableRegion(new RectF(5, 10 + (index * 55), 40,
                    55 + (index * 55)), 0, check));
            // draw text
            layers.get(LAYER_BACKGROUND).addRegion(new TextRegion(new RectF(45, 32 + (index * 55), this.width, 0), 0,
                    config.getTitle(), textPaint, Alignment.ALIGN_NORMAL));
            index++;
        }
    }

    public void addStartButtonRegions(boolean isMaster) {
        if (width == 0 || height == 0) {
            return;
        }
        Drawable npd = isMaster ? BoardViewSurfaceThread.buttonEnabled : BoardViewSurfaceThread.buttonDisabled;
        RectF bounds = new RectF(60, height - 80, width - 60, height - 5);
        RectF textBounds = new RectF(bounds);
        textBounds.offset(0, bounds.height() / 2);
        layers.get(LAYER_BACKGROUND).addRegion(new DrawableRegion(bounds, 0, npd));
        layers.get(LAYER_BACKGROUND).addRegion(new TextRegion(textBounds, 0, "Start Game", isMaster ? BoardViewSurfaceThread.textNegativePaint : BoardViewSurfaceThread.textPaint,
                    Alignment.ALIGN_CENTER));
        if (isMaster) {
            layers.get(LAYER_BACKGROUND).addRegion(new GestureRegion(bounds, TAG_START_BUTTON, BoardViewSurfaceThread.highlightPaint, new GestureRegionListener() {

                @Override
                public void onRegionLongPress(Region region) {
                    // do nothing on start button long press
                }

                @Override
                public void onRegionClick(Region region) {
                    thisListener.onStartGame();
                }
            }));
        }
    }

    public void addEstateRegions(ArrayList<Estate> estates) {
        for (int i = 0; i < 40; i++) {
            addEstateRegion(i, estates.get(i));
        }
    }
    
    private void addEstateRegion(int index, Estate estate) {
        // calculate the regions...
        float part = ((float)width / ((4f * phi) + 18f));
        float estateX = 0, estateY = 0, estateW = 0, estateH = 0;
        float gradX = 0, gradY = 0, gradW = 0, gradH = 0;
        float iconX = 0, iconY = 0, pieceX = 0, pieceY = 0, pieceDeltaX = 0, pieceDeltaY = 0;
        PointF grad1 = null, grad2 = null;
        if (range(index, 0, 10)) {
            gradW = estateW = 2;
            gradH = estateH = (2f * phi);
            gradX = estateX = ((4f * phi) + 18f) - 2f * phi - (float)index * 2f;
            gradY = estateY = ((4f * phi) + 18f) - (2f * phi);
            grad1 = new PointF(estateX * part, estateY * part);
            grad2 = new PointF((estateX + estateW) * part, estateY * part);
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
            grad1 = new PointF(estateX * part, estateY * part);
            grad2 = new PointF(estateX * part, (estateY + estateH) * part);
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
            grad1 = new PointF((estateX + estateW) * part, estateY * part);
            grad2 = new PointF(estateX * part, estateY * part);
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
            grad1 = new PointF(estateX * part, (estateY + estateH) * part);
            grad2 = new PointF(estateX * part, estateY * part);
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
        RectF bounds = new RectF(estateX, estateY, estateX + estateW, estateY + estateH);
        estateBgPaint.setColor(estate.getBgColor());
        layers.get(LAYER_BACKGROUND).addRegion(new BorderedRegion(bounds, 0, new Paint(estateBgPaint), estateBorderPaint));
        estate.setDrawRegion(bounds);
        estate.setDrawOwnerLocation(new PointF(iconX, iconY));
        estate.setPieceLocation(new PointF(pieceX, pieceY));
        estate.setPieceLocationOffset(new PointF(pieceDeltaX, pieceDeltaY));
        estate.setDrawRadius(10f);
        if (estate.getColor() != 0) {
            Paint grad = new Paint(estateBgPaint);
            grad.setColor(estate.getColor());
            RectF gradBounds = new RectF(gradX, gradY, gradX + gradW, gradY + gradH);
            grad.setShader(new LinearGradient(grad1.x, grad1.y, grad2.x, grad2.y, darken(grad.getColor()), grad.getColor(), Shader.TileMode.CLAMP));
            estateBgPaint.setColor(estate.getColor());
            layers.get(LAYER_BACKGROUND).addRegion(new BorderedRegion(gradBounds, 0, grad, estateBorderPaint));
            estate.setDrawOwnerRegion(gradBounds);
        }
    }

    public void addEstateHouseRegions(ArrayList<Estate> estates, SparseArray<Player> players) {
        if (width == 0 || height == 0) {
            return;
        }
        for (int i = 0; i < 40; i++) {
            Estate estate = estates.get(i);
            if (estate.canBeOwned()) {
                int owner = estate.getOwner();
                if (owner > 0) {
                    Player player = players.get(owner);
                    PointF piece = estate.getDrawOwnerLocation();
                    float radius = estate.getDrawRadius();
                    RectF rect = new RectF(piece.x - radius, piece.y - radius, piece.x + radius, piece.y + radius);
                    Paint paint = new Paint(playerPaint);
                    paint.setColor(player.getDrawColor());
                    if (estate.isMortgaged()) {
                        paint.setColor(Color.argb(
                                paint.getAlpha() / 2,
                                Color.red(paint.getColor()),
                                Color.green(paint.getColor()),
                                Color.blue(paint.getColor())));
                    }
                    layers.get(LAYER_OWNERS).addRegion(new EstateStarRegion(rect, 0, paint));
                }
            }
        }
    }

    public void addPieceRegions(ArrayList<Estate> estates, int[] playerIds, SparseArray<Player> players) {
        if (width == 0 || height == 0) {
            return;
        }

        for (int i = 0; i < 40; i++) {
            int[] pieces = new int[4];
            Estate estate = estates.get(i);
            int pieceCount = 0;
            for (int playerIndex = 0; playerIndex < 4; playerIndex++) {
                int playerId = playerIds[playerIndex];
                if (playerId > 0) {
                    Player player = players.get(playerId);
                    if (player.getDrawColor() != 0) {
                        if (player.getDrawLocation() == i) {
                            pieces[pieceCount] = playerId;
                            pieceCount++;
                        }
                    }
                }
            }
            for (int pieceIndex = 0; pieceIndex < 4 && pieceIndex < pieceCount; pieceIndex++) {
                PointF location = new PointF();
                PointF offset = estate.getDrawPieceLocationOffset();
                float radius = estate.getDrawRadius();
                float offsetScale = pieceIndex - (pieceCount - 1) / 2f;
                int playerId = pieces[pieceIndex];
                Player player = players.get(playerId);
                location.set(estate.getDrawPieceLocation());
                location.offset(offset.x * offsetScale, offset.y * offsetScale);
                RectF rect = new RectF(location.x - radius, location.y - radius, location.x + radius, location.y + radius);
                layers.get(LAYER_PIECES).addRegion(new DrawableRegion(rect, 0, createRadialGradient(player.getDrawColor())));
            }
        }
    }

    /**
     * Enables or disables all region buttons in LAYER_BACKGROUND (where the estate buttons are).
     * @param enabled Whether to enable the GestureRegions.
     */
    public void setEstateButtonsEnabled(boolean enabled) {
        for (Region region : layers.get(LAYER_BACKGROUND).getRegions()) {
            if (region instanceof GestureRegion) {
                GestureRegion greg = (GestureRegion) region;
                greg.setEnabled(enabled);
            }
        }
    }

    public void addOverlayRegion() {
        if (width == 0 || height == 0) {
            return;
        }
        overlayPaint.setShader(
                new RadialGradient(width / 2, height / 2, width / 4, Color.argb(128, 0, 0, 0), Color.argb(0, 0, 0, 0), Shader.TileMode.CLAMP));
        layers.get(LAYER_OVERLAY).addRegion(new BorderedRegion(new RectF(0, 0, width, height), 0, overlayPaint, null));
    }

    public void addPlayerOverlayRegions(Player player) {
        if (width == 0 || height == 0) {
            return;
        }
    }

    public void addEstateOverlayRegions(Estate estate) {
        if (width == 0 || height == 0) {
            return;
        }
    }

    public void addTurnRegions(int[] playerIds, SparseArray<Player> players) {
        if (width == 0 || height == 0) {
            return;
        }
        int y = height / 6;
        for (int playerId : playerIds) {
            if (playerId > 0) {
                Player player = players.get(playerId);
                if (player.isTurn()) {
                    layers.get(LAYER_OWNERS).addRegion(new TextRegion(new RectF(0, y, width, height), 0, "It is " + player.getNick() + "'s turn.", textPaint, Alignment.ALIGN_CENTER));
                }
            }
        }
    }

    public static GradientDrawable createRadialGradient(int color) {
        GradientDrawable grad = gradCache.get(color);
        if (grad == null) {
            grad = new GradientDrawable(Orientation.LEFT_RIGHT, new int[] { color, darken(color) });
            grad.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            grad.setShape(GradientDrawable.OVAL);
            grad.setSize(25, 25);
            grad.setGradientRadius(grad.getIntrinsicWidth() / 2f);
            //grad.setGradientCenter(grad.getIntrinsicWidth() / 3f, grad.getIntrinsicHeight() / 3f);
            gradCache.put(color, grad);
        }
        return grad;
    }

    private static int darken(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] -= 0.4;
        return Color.HSVToColor(hsv);
    }
}
