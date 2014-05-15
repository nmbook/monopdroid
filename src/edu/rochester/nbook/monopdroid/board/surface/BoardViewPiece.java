package edu.rochester.nbook.monopdroid.board.surface;

import java.util.HashMap;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;

/**
 * Represents one of four pieces.
 * 1=GREEN, 2=RED, 3=CYAN, 4=YELLOW, 5=PURPLE, 6=
 * 
 * @author Nate
 *
 */
public class BoardViewPiece {
    public static final int MAX_PLAYERS = 8;
    public static final BoardViewPiece[] pieces = new BoardViewPiece[MAX_PLAYERS];
    
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
    private int playerId;
     /**
     * The estate ID the piece is currently on.
     */
    private int currentEstate;
    /**
     * The estate ID of the piece as it progresses in a non-directMove.
     */
    private int progressEstate;
    /**
     * The amount of progress (between 0 and anim_ms setting).
     */
    private int progressEstateDelta;
    
    public BoardViewPiece(int index) {
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
    
    public int getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }
    
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
            return Color.rgb(255, 127, 0); // orange, sucker
        }
    }

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
        }
    }
    
    public GradientDrawable getDrawable(For key) {
        return draw.get(key);
    }
    
    public int getCurrentEstate() {
        return currentEstate;
    }
    
    public void setCurrentEstate(int currentEstate) {
        this.currentEstate = currentEstate;
    }
    
    public int getProgressEstate() {
        return progressEstate;
    }
    
    public void setProgressEstate(int progressEstate) {
        this.progressEstate = progressEstate;
    }
    
    public int getProgressEstateDelta() {
        return progressEstateDelta;
    }
    
    public void setProgressEstateDelta(int progressEstateDelta) {
        this.progressEstateDelta = progressEstateDelta;
    }

    public static BoardViewPiece getPiece(int playerId) {
        for (int j = 0; j < MAX_PLAYERS; j++) {
            if (playerId == BoardViewPiece.pieces[j].getPlayerId()) {
                return BoardViewPiece.pieces[j];
            }
        }
        return null;
    }

    public int getIndex() {
        return pieceIndex - 1;
    }
}
