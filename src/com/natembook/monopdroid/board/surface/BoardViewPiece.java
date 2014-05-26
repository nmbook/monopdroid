package com.natembook.monopdroid.board.surface;

import java.util.HashMap;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;

/**
 * Represents one of eight pieces.
 * 1=GREEN, 2=RED, 3=CYAN, 4=YELLOW, 5=PURPLE, 6=BLUE, 7=GRAY, 8=ORANGE
 * 
 * @author Nate
 *
 */
public class BoardViewPiece {
    /**
     * Represents the maximum number of players this app supports.
     * To modify this, you must also modify the number of layouts in the players.xml
     * layout and related layout code.
     */
    public static final int MAX_PLAYERS = 8;
    /**
     * The current list of player pieces.
     * This will be statically initialized and filled with proper-color pieces.
     * To use a piece, the code associates a player ID with it,
     * and to render, it may associate an estate ID and animation progress hints.
     */
    public static final BoardViewPiece[] pieces = new BoardViewPiece[MAX_PLAYERS];
    
    /**
     * Places where a piece can be rendered at the same time on one SurfaceView.
     * @author Nate
     */
    public enum For {
        PLAYER_LIST, BOARD, OVERLAY
    }
    
    private HashMap<For, GradientDrawable> draw = new HashMap<For, GradientDrawable>();
    
    static {
        pieces[0] = new BoardViewPiece(1);
        pieces[1] = new BoardViewPiece(2);
        pieces[2] = new BoardViewPiece(3);
        pieces[3] = new BoardViewPiece(4);
        pieces[4] = new BoardViewPiece(5);
        pieces[5] = new BoardViewPiece(6);
        pieces[6] = new BoardViewPiece(7);
        pieces[7] = new BoardViewPiece(8);
    }
    
    /**
     * The index of the piece. 1-8.
     */
    private int pieceIndex;
    /**
     * The drawable to draw for the piece.
     */
    private GradientDrawable drawable;
    
    /**
     * The player ID using the piece.
     */
    private int playerId = -1;
     /**
     * The estate ID the piece is currently on.
     */
    private int currentEstate = 0;
    /**
     * The estate ID of the piece as it progresses in a non-directMove.
     */
    private int progressEstate = 0;
    /**
     * The amount of progress (between 0 and anim_ms setting).
     */
    private int progressEstateDelta = 0;
    
    /**
     * Whether this piece is in motion.
     */
    private boolean moving = false;
    
    /**
     * Create a Piece with the given index.
     * @param index 1-based player number
     */
    private BoardViewPiece(int index) {
        this.pieceIndex = index;
        int color = getColor();
        for (For key : For.values()) {
            drawable = new GradientDrawable(Orientation.LEFT_RIGHT, new int[] { color, BoardViewSurfaceThread.darken(color) });
            drawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setSize(25, 25);
            drawable.setBounds(0, 0, 25, 25);
            drawable.setGradientRadius(drawable.getIntrinsicWidth() / 2f);
            //drawable.setGradientCenter(drawable.getIntrinsicWidth() / 3f, drawable.getIntrinsicHeight() / 3f);
            draw.put(key, drawable);
        }
    }
    
    /**
     * Get the player ID associated with this piece.
     * @return A player ID
     */
    public int getPlayerId() {
        return playerId;
    }
    
    /**
     * Set the player ID associated with this piece.
     * @param playerId A player ID
     */
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }
    
    /**
     * Get the hard-coded color of this piece, based on our index/player number
     * @return Green, Red, Cyan, Yellow, Magenta, Gray, or Orange
     */
    public int getColor() {
        switch (pieceIndex) {
        default:
        case 1:
            return Color.GREEN;
        case 2:
            return Color.RED;
        case 3:
            return Color.CYAN;
        case 4:
            return Color.YELLOW;
        case 5:
            return Color.MAGENTA;
        case 6:
            return Color.BLUE;
        case 7:
            return Color.LTGRAY;
        case 8:
            return Color.rgb(255, 127, 0); // orange
        }
    }

    /**
     * Get the English name of the piece color as retrieved from {@code getColor()}.
     * @return Green, Red, Cyan, Yellow, Magenta, Gray, or Orange
     */
    public String getColorName() {
        switch (pieceIndex) {
        default:
        case 1:
            return "Green";
        case 2:
            return "Red";
        case 3:
            return "Cyan";
        case 4:
            return "Yellow";
        case 5:
            return "Magenta";
        case 6:
            return "Blue";
        case 7:
            return "Gray";
        case 8:
            return "Orange";
        }
    }
    
    /**
     * Get a drawable to draw of this piece.
     * @param key The type of drawable to get
     * @return The requested drawable, or null if no such drawable exists.
     */
    public GradientDrawable getDrawable(For key) {
        return draw.get(key);
    }
    
    /**
     * Get the current estate that this piece is on.
     * @return An Estate ID
     */
    public int getCurrentEstate() {
        return currentEstate;
    }
    
    /**
     * Set the current estate that this piece is on.
     * @param currentEstate An Estate ID
     */
    public void setCurrentEstate(int currentEstate) {
        this.currentEstate = currentEstate;
    }
    
    /**
     * Get the intermediate location of the piece in an animation. 
     * @return An Estate ID
     */
    public int getProgressEstate() {
        return progressEstate;
    }

    /**
     * Set the intermediate location of the piece in an animation.
     * @param currentEstate An Estate ID
     */
    public void setProgressEstate(int progressEstate) {
        this.progressEstate = progressEstate;
    }
    
    /**
     * Get a value between 0 and animation_steps indicating the progress of the animation across this space on the board.
     * @return A number from 0 to animation_steps - 1
     */
    public int getProgressEstateDelta() {
        return progressEstateDelta;
    }

    /**
     * Set a value between 0 and animation_steps indicating the progress of the animation across this space on the board.
     * @param progressEstateDelta A number from 0 to animation_steps - 1
     */
    public void setProgressEstateDelta(int progressEstateDelta) {
        this.progressEstateDelta = progressEstateDelta;
    }
    
    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public boolean isMoving() {
        return moving;
    }

    /**
     * Get a piece by player ID
     * @param playerId The player ID
     * @return A piece object. If that player ID doesn't have a piece, return null
     */
    public static BoardViewPiece getPiece(int playerId) {
        for (BoardViewPiece piece : pieces) {
            if (playerId == piece.getPlayerId()) {
                return piece;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return getColorName() + " piece for player id " + playerId + " (index: " + pieceIndex + ")";
    }

    public static int getIndexOf(int playerId) {
        for (int i = 0; i < BoardViewPiece.MAX_PLAYERS; i++) {
            if (BoardViewPiece.pieces[i].getPlayerId() == playerId) {
                return i;
            }
        }
        return 0;
    }
}
