package edu.rochester.nbook.monopdroid;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.NinePatchDrawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class BoardView extends SurfaceView implements Runnable {
    public enum DrawState {
        WAIT_JOIN, WAIT_CREATE, CONFIG, INIT, RUN, END
    }

    public interface BoardViewListener {
        public void onConfigChange(String command, String value);

        public void onStartGame();
    }

    private interface RegionListener {
        public void onRegionClick(Region region);

        public void onRegionLongPress(Region region);
    }

    private abstract class Region {
        private String tag;
        private Rect bounds;

        public Region(String tag, Rect bounds) {
            this.tag = tag;
            this.bounds = bounds;
        }

        public String getTag() {
            return this.tag;
        }

        public Rect getBounds() {
            return this.bounds;
        }

        public abstract void draw(Canvas canvas);
    }

    private class TextRegion extends Region {
        private StaticLayout layout;

        public TextRegion(String text, Rect bounds, Paint textPaint, Alignment align) {
            super(text, bounds);
            this.layout = new StaticLayout(text, new TextPaint(textPaint), this.getBounds().right
                            - this.getBounds().left, align, 1f, 0f, false);
            super.bounds.top = super.bounds.top - this.layout.getHeight() / 2;
            super.bounds.bottom = super.bounds.top + this.layout.getHeight();
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.save();
            canvas.translate(this.getBounds().left, this.getBounds().top);
            this.layout.draw(canvas);
            canvas.restore();
            // canvas.drawText(this.getTag(), this.getBounds().left,
            // this.getBounds().top + 30, this.textPaint);
        }
    }

    private class DrawableRegion extends Region {
        private Paint imgPaint;
        private Bitmap bmp;

        public DrawableRegion(String tag, Rect bounds, Bitmap bitmap, Paint imgPaint) {
            super(tag, bounds);
            this.bmp = bitmap;
            this.imgPaint = imgPaint;
        }

        public void setBitmap(Bitmap bmp) {
            this.bmp = bmp;
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawBitmap(this.bmp, null, this.getBounds(), this.imgPaint);
        }
    }

    private class NinePatchDrawableRegion extends Region {
        private NinePatchDrawable npd;

        public NinePatchDrawableRegion(String tag, Rect bounds, NinePatchDrawable npd) {
            super(tag, bounds);
            this.npd = npd;
            this.npd.setBounds(super.bounds);
        }

        @Override
        public void draw(Canvas canvas) {
            this.npd.draw(canvas);
        }
    }

    private class GestureRegion extends Region {
        private RegionListener listener;
        private boolean isPressed;
        private Paint bgPaint;

        public GestureRegion(String tag, Rect bounds, Paint paint, RegionListener listener) {
            super(tag, bounds);
            this.listener = listener;
            this.bgPaint = paint;
        }

        @Override
        public void draw(Canvas canvas) {
            if (this.isPressed) {
                canvas.drawRoundRect(new RectF(this.getBounds()), 4f, 4f, this.bgPaint);
            }
        }

        public void onDown() {
            this.isPressed = true;
        }

        public void onUp() {
            this.isPressed = false;
        }

        public void invokeClick() {
            this.listener.onRegionClick(this);
        }

        public void invokeLongPress() {
            this.listener.onRegionLongPress(this);
        }
    }

    // ui thread exclusives
    private Thread surfaceThread = null;
    private SurfaceHolder surfaceHolder = null;
    private ScaleGestureDetector scaleDetector = null;
    private GestureDetector scrollDetector = null;
    private BoardViewListener thisListener = null;

    // draw thread cached images
    private static Bitmap checkIconChecked = null;
    private static Bitmap checkIconUnchecked = null;
    private static Bitmap checkIconCheckedFocused = null;
    private static Bitmap checkIconUncheckedFocused = null;
    private static Bitmap checkIconCheckedDisabled = null;
    private static Bitmap checkIconUncheckedDisabled = null;
    private static NinePatchDrawable buttonEnabled = null;
    private static NinePatchDrawable buttonDisabled = null;

    // draw thread exclusives (cached data and scale state)
    private Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textNegativePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // used to store draw state across threads
    private volatile boolean running = false;
    private volatile DrawState state = DrawState.WAIT_CREATE;
    private volatile List<Region> regions = new ArrayList<BoardView.Region>();
    private volatile List<Estate> estates = new ArrayList<Estate>();
    private volatile List<Configurable> config = new ArrayList<Configurable>();
    private volatile List<Player> player = new ArrayList<Player>();
    private volatile float offsetX = 0f;
    private volatile float offsetY = 0f;
    private volatile float scale = 1f;

    // used by gesture objects to enable/disable scaling/translation
    private float maxScale = 1f;
    private float minScale = 1f;
    private boolean enableTranslate = false;
    private boolean enableScale = false;

    public void setBoardViewListener(BoardViewListener listener) {
        this.thisListener = listener;
    }

    public DrawState getState() {
        return this.state;
    }

    public void setState(DrawState state) {
        this.state = state;
        Log.d("monopd", "surface: state = " + state.toString());
        synchronized (this.regions) {
            this.regions.clear();
            this.offsetX = this.offsetY = 0f;
            this.scale = 1f;
            this.enableTranslate = false;
            this.enableScale = false;
            switch (state) {
            case WAIT_CREATE:
                this.regions.add(new TextRegion("Creating game...", new Rect(this.getWidth() / 3, this.getHeight() / 2,
                                this.getWidth(), this.getHeight()), this.textPaint, Alignment.ALIGN_NORMAL));
                break;
            case WAIT_JOIN:
                this.regions.add(new TextRegion("Joining game...", new Rect(this.getWidth() / 3, this.getHeight() / 2,
                                this.getWidth(), this.getHeight()), this.textPaint, Alignment.ALIGN_NORMAL));
                break;
            case CONFIG:
                this.enableTranslate = false;
                this.enableScale = false;
                break;
            case INIT:
                this.regions.add(new TextRegion("Starting game...", new Rect(this.getWidth() / 3, this.getHeight() / 2,
                                this.getWidth(), this.getHeight()), this.textPaint, Alignment.ALIGN_NORMAL));
                break;
            case RUN:
                this.enableTranslate = true;
                this.enableScale = true;
                break;
            case END:
                this.enableTranslate = true;
                this.enableScale = true;
                break;
            }
        }
    }

    public void setConfigurables(List<Configurable> configurables, boolean isMaster) {
        this.config = configurables;

        synchronized (this.regions) {
            this.regions.clear();
            this.addStartButtonRegions(isMaster);
            int index = 0;
            for (Configurable config : configurables) {
                Bitmap check = null;
                if (config.isEditable()) {
                    if (config.getValue().equals("0")) {
                        check = checkIconUnchecked;
                    } else {
                        check = checkIconChecked;
                    }
                    this.regions.add(new GestureRegion(config.getCommand(), new Rect(0, 5 + (index * 55), this
                                    .getWidth(), 60 + (index * 55)), this.highlightPaint, new RegionListener() {
                        @Override
                        public void onRegionLongPress(Region region) {
                            // do nthing on check box long-press
                        }

                        @Override
                        public void onRegionClick(Region region) {
                            if (BoardView.this.thisListener != null) {
                                for (Configurable currentConfigurable : BoardView.this.config) {
                                    if (currentConfigurable.getCommand().equals(region.getTag())) {
                                        BoardView.this.thisListener.onConfigChange(currentConfigurable.getCommand(),
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
                this.regions.add(new DrawableRegion("check-" + config.getCommand(), new Rect(5, 10 + (index * 55), 40,
                                55 + (index * 55)), check, this.textPaint));
                // draw text
                this.regions.add(new TextRegion(config.getTitle(), new Rect(45, 32 + (index * 55), this.getWidth(), 0),
                                this.textPaint, Alignment.ALIGN_NORMAL));
                index++;
            }
        }
    }

    private void addStartButtonRegions(boolean isMaster) {
        NinePatchDrawable npd = isMaster ? buttonEnabled : buttonDisabled;
        Rect bounds = new Rect(60, this.getHeight() - 90, this.getWidth() - 60, this.getHeight());
        Rect textBounds = new Rect(bounds);
        textBounds.offset(0, bounds.height() / 2);
        this.regions.add(new NinePatchDrawableRegion("btn-start", bounds, npd));
        this.regions.add(new TextRegion("Start Game", textBounds, isMaster ? this.textNegativePaint : this.textPaint,
                        Alignment.ALIGN_CENTER));
        if (isMaster) {
            this.regions.add(new GestureRegion(null, bounds, this.highlightPaint, new RegionListener() {
                @Override
                public void onRegionLongPress(Region region) {
                }

                @Override
                public void onRegionClick(Region region) {
                    BoardView.this.thisListener.onStartGame();
                }
            }));
        }
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.privateInit();
    }

    public BoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.privateInit();
    }

    private void privateInit() {
        this.surfaceHolder = this.getHolder();
        // spaces = new ArrayList<BoardView.Space>(40);
        this.scrollDetector = new GestureDetector(new GestureDetector.OnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                int x = (int) e.getX() - (int) BoardView.this.offsetX;
                int y = (int) e.getY() - (int) BoardView.this.offsetY;
                synchronized (BoardView.this.regions) {
                    for (Region region : BoardView.this.regions) {
                        if (region instanceof GestureRegion) {
                            GestureRegion greg = (GestureRegion) region;
                            if (greg.getBounds().contains(x, y)) {
                                greg.invokeClick();
                                greg.onUp();
                                return true;
                            }
                        }
                    }
                }
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                int x = (int) e.getX() - (int) BoardView.this.offsetX;
                int y = (int) e.getY() - (int) BoardView.this.offsetY;
                synchronized (BoardView.this.regions) {
                    for (Region region : BoardView.this.regions) {
                        if (region instanceof GestureRegion) {
                            GestureRegion greg = (GestureRegion) region;
                            if (greg.getBounds().contains(x, y)) {
                                greg.onDown();
                                return;
                            }
                        }
                    }
                }
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (BoardView.this.enableTranslate) {
                    BoardView.this.offsetX -= distanceX;
                    BoardView.this.offsetY -= distanceY;
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onLongPress(MotionEvent e) {
                int x = (int) e.getX() - (int) BoardView.this.offsetX;
                int y = (int) e.getY() - (int) BoardView.this.offsetY;
                synchronized (BoardView.this.regions) {
                    for (Region region : BoardView.this.regions) {
                        if (region instanceof GestureRegion) {
                            GestureRegion greg = (GestureRegion) region;
                            if (greg.getBounds().contains(x, y)) {
                                greg.invokeLongPress();
                                greg.onUp();
                                return;
                            }
                        }
                    }
                }
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
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.scrollDetector.onTouchEvent(event);
    }

    public void onResume() {
        this.running = true;
        this.surfaceThread = new Thread(this);
        this.surfaceThread.start();
    }

    public void onPause() {
        boolean retry = true;
        this.running = false;
        while (retry) {
            try {
                this.surfaceThread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
    }

    @Override
    public void run() {
        Log.d("monopd", "surface: Surface thread init");
        // init cached data
        this.bgPaint.setStyle(Style.FILL);
        this.bgPaint.setColor(Color.BLACK);
        this.textPaint.setStyle(Style.STROKE);
        this.textPaint.setColor(Color.WHITE);
        this.textPaint.setTextSize(24);
        this.textNegativePaint.setColor(Color.BLACK);
        this.textNegativePaint.setTextSize(24);
        this.highlightPaint.setStyle(Style.STROKE);
        this.highlightPaint.setColor(Color.YELLOW);
        this.highlightPaint.setStrokeWidth(2f);
        checkIconChecked = BitmapFactory.decodeResource(this.getResources(), R.drawable.btn_check_on);
        checkIconUnchecked = BitmapFactory.decodeResource(this.getResources(), R.drawable.btn_check_off);
        checkIconCheckedDisabled = BitmapFactory.decodeResource(this.getResources(), R.drawable.btn_check_on_disable);
        checkIconUncheckedDisabled = BitmapFactory
                        .decodeResource(this.getResources(), R.drawable.btn_check_off_disable);
        checkIconCheckedFocused = BitmapFactory.decodeResource(this.getResources(), R.drawable.btn_check_on_selected);
        checkIconUncheckedFocused = BitmapFactory
                        .decodeResource(this.getResources(), R.drawable.btn_check_off_selected);
        buttonEnabled = (NinePatchDrawable) this.getResources().getDrawable(R.drawable.btn_default_normal);
        buttonDisabled = (NinePatchDrawable) this.getResources().getDrawable(R.drawable.btn_default_normal_disable);

        Log.d("monopd", "surface: Completed surface set-up");

        // Paint oPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // oPaint.setColor(Color.YELLOW);
        // oPaint.setStyle(Paint.Style.STROKE);
        while (this.running) {
            if (this.surfaceHolder.getSurface().isValid()) {
                Canvas canvas = this.surfaceHolder.lockCanvas();
                canvas.drawRect(new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), this.bgPaint);
                canvas.scale(this.scale, this.scale);
                canvas.translate(this.offsetX, this.offsetY);
                synchronized (this.regions) {
                    for (Region region : this.regions) {
                        // canvas.drawRect(region.getBounds(), oPaint);
                        region.draw(canvas);
                    }
                }
                this.surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
